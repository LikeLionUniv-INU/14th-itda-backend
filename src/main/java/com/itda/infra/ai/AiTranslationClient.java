package com.itda.infra.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiTranslationClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public AiTranslationClient(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public List<TranslatedItem> translate(List<TranslationInput> items, String sourceLanguage, String targetLanguage) {
        try {
            String itemsJson = objectMapper.writeValueAsString(items);

            String systemPrompt = String.format(
                    "You are a professional technical document translator. " +
                    "Translate the following requirement items from %s to %s. " +
                    "Maintain technical terminology accuracy. " +
                    "Return ONLY a JSON array with the same structure: " +
                    "[{\"id\": <number>, \"itemName\": \"<translated>\", \"content\": \"<translated>\"}]. " +
                    "Do not add any explanation or markdown formatting. Return raw JSON only.",
                    sourceLanguage, targetLanguage
            );

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", itemsJson)
                    ),
                    "temperature", 0.3
            );

            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            String jsonContent = content.strip();
            if (jsonContent.startsWith("```")) {
                jsonContent = jsonContent.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").strip();
            }

            return objectMapper.readValue(jsonContent, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("AI 번역 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 번역 중 오류가 발생했습니다.", e);
        }
    }

    public record TranslationInput(Long id, String itemName, String content) {}

    public record TranslatedItem(Long id, String itemName, String content) {}
}
