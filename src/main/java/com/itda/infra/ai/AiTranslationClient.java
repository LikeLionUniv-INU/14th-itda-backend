package com.itda.infra.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AiTranslationClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    private static final int MAX_RETRIES = 3;
    private static final int CHUNK_SIZE = 30;
    private static final long[] RETRY_DELAYS_MS = {1000, 2000, 4000};

    public AiTranslationClient(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            ObjectMapper objectMapper) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);

        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public List<TranslatedItem> translate(List<TranslationInput> items,
                                           String sourceLanguage,
                                           String targetLanguage) {
        if (items.isEmpty()) {
            return List.of();
        }

        // 대량 요구사항은 청크 분할 처리
        if (items.size() > CHUNK_SIZE) {
            return translateInChunks(items, sourceLanguage, targetLanguage);
        }

        return translateWithRetry(items, sourceLanguage, targetLanguage);
    }

    private List<TranslatedItem> translateInChunks(List<TranslationInput> items,
                                                    String sourceLanguage,
                                                    String targetLanguage) {
        List<TranslatedItem> allResults = new ArrayList<>();

        for (int i = 0; i < items.size(); i += CHUNK_SIZE) {
            List<TranslationInput> chunk = items.subList(i,
                    Math.min(i + CHUNK_SIZE, items.size()));
            log.info("번역 청크 처리 중: {}/{}", i + chunk.size(), items.size());
            allResults.addAll(translateWithRetry(chunk, sourceLanguage, targetLanguage));
        }

        return allResults;
    }

    private List<TranslatedItem> translateWithRetry(List<TranslationInput> items,
                                                     String sourceLanguage,
                                                     String targetLanguage) {
        Exception lastException = null;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                if (attempt > 0) {
                    long delay = RETRY_DELAYS_MS[attempt - 1];
                    log.warn("번역 재시도 {}/{}, {}ms 대기", attempt + 1, MAX_RETRIES, delay);
                    Thread.sleep(delay);
                }

                List<TranslatedItem> result = callOpenAi(items, sourceLanguage, targetLanguage);
                List<TranslatedItem> validated = validateAndRepair(result, items);
                return validated;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("번역 중 인터럽트 발생", e);
            } catch (Exception e) {
                lastException = e;
                log.warn("번역 시도 {}/{} 실패: {}", attempt + 1, MAX_RETRIES, e.getMessage());
            }
        }

        throw new RuntimeException("AI 번역 " + MAX_RETRIES + "회 재시도 후 최종 실패", lastException);
    }

    private List<TranslatedItem> callOpenAi(List<TranslationInput> items,
                                             String sourceLanguage,
                                             String targetLanguage) throws Exception {
        String itemsJson = objectMapper.writeValueAsString(items);

        String systemPrompt = String.format(
                "You are a professional technical document translator. " +
                "Translate the following requirement items from %s to %s. " +
                "Maintain technical terminology accuracy. " +
                "You MUST return a JSON object with a single key \"items\" containing an array. " +
                "Each element MUST have: \"id\" (number, same as input), \"itemName\" (string), \"content\" (string). " +
                "Translate EVERY item. Do NOT skip any. The output array length MUST equal the input array length.",
                sourceLanguage, targetLanguage
        );

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", itemsJson)
        ));
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 4096);
        requestBody.put("response_format", buildJsonSchema());

        String responseBody = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseBody);

        // finish_reason 체크 — 토큰 초과로 응답이 잘렸는지 확인
        String finishReason = root.path("choices").get(0).path("finish_reason").asText();
        if ("length".equals(finishReason)) {
            throw new RuntimeException("OpenAI 응답이 토큰 한도 초과로 잘렸습니다 (finish_reason=length)");
        }

        String content = root.path("choices").get(0).path("message").path("content").asText();

        String jsonContent = content.strip();
        if (jsonContent.startsWith("```")) {
            jsonContent = jsonContent.replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "").strip();
        }

        // response_format=json_object는 {"items": [...]} 형태로 반환
        JsonNode parsed = objectMapper.readTree(jsonContent);

        // "items" 키가 있으면 그 안의 배열 사용, 없으면 최상위가 배열인지 확인
        JsonNode arrayNode;
        if (parsed.has("items")) {
            arrayNode = parsed.get("items");
        } else if (parsed.isArray()) {
            arrayNode = parsed;
        } else {
            // 다른 키로 감싸져 있는 경우 첫 번째 배열 필드를 찾음
            arrayNode = null;
            Iterator<Map.Entry<String, JsonNode>> fields = parsed.fields();
            while (fields.hasNext()) {
                JsonNode value = fields.next().getValue();
                if (value.isArray()) {
                    arrayNode = value;
                    break;
                }
            }
            if (arrayNode == null) {
                throw new RuntimeException("OpenAI 응답에서 번역 배열을 찾을 수 없습니다: " + jsonContent);
            }
        }

        return objectMapper.readValue(arrayNode.toString(), new TypeReference<>() {});
    }

    private List<TranslatedItem> validateAndRepair(List<TranslatedItem> results,
                                                    List<TranslationInput> inputs) {
        // 입력 ID 기준으로 결과를 매핑
        Map<Long, TranslatedItem> resultMap = results.stream()
                .filter(r -> r.id() != null)
                .collect(Collectors.toMap(
                        TranslatedItem::id,
                        r -> r,
                        (a, b) -> a  // 중복 시 첫 번째 사용
                ));

        List<TranslatedItem> validated = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();

        for (TranslationInput input : inputs) {
            TranslatedItem translated = resultMap.get(input.id());
            if (translated != null) {
                validated.add(translated);
            } else {
                // AI가 누락한 항목 → 원본 유지
                missingIds.add(input.id());
                validated.add(new TranslatedItem(input.id(), input.itemName(), input.content()));
            }
        }

        if (!missingIds.isEmpty()) {
            log.warn("번역 결과에서 누락된 ID (원본 유지): {}", missingIds);
        }

        return validated;
    }

    private Map<String, Object> buildJsonSchema() {
        Map<String, Object> itemProperties = new LinkedHashMap<>();
        itemProperties.put("id", Map.of("type", "number"));
        itemProperties.put("itemName", Map.of("type", "string"));
        itemProperties.put("content", Map.of("type", "string"));

        Map<String, Object> itemSchema = new LinkedHashMap<>();
        itemSchema.put("type", "object");
        itemSchema.put("properties", itemProperties);
        itemSchema.put("required", List.of("id", "itemName", "content"));
        itemSchema.put("additionalProperties", false);

        Map<String, Object> rootProperties = new LinkedHashMap<>();
        rootProperties.put("items", Map.of("type", "array", "items", itemSchema));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", rootProperties);
        schema.put("required", List.of("items"));
        schema.put("additionalProperties", false);

        Map<String, Object> jsonSchema = new LinkedHashMap<>();
        jsonSchema.put("name", "translation_result");
        jsonSchema.put("strict", true);
        jsonSchema.put("schema", schema);

        Map<String, Object> responseFormat = new LinkedHashMap<>();
        responseFormat.put("type", "json_schema");
        responseFormat.put("json_schema", jsonSchema);

        return responseFormat;
    }

    public record TranslationInput(Long id, String itemName, String content) {}

    public record TranslatedItem(Long id, String itemName, String content) {}
}
