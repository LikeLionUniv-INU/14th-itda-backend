package com.itda.domain.translation.controller;

import com.itda.domain.translation.dto.request.TranslateRequest;
import com.itda.domain.translation.dto.response.TranslationJobResponse;
import com.itda.domain.translation.service.TranslationService;
import com.itda.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;
    private final RedisMessageListenerContainer listenerContainer;

    @PostMapping("/api/documents/{documentId}/versions/{version}/translate")
    public ResponseEntity<ApiResponse<TranslationJobResponse>> requestTranslation(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @Valid @RequestBody TranslateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        TranslationJobResponse response = translationService.requestTranslation(
                userId, documentId, version, request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok("번역이 요청되었습니다.", response));
    }

    @GetMapping(value = "/api/translations/{jobId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTranslationStatus(
            Authentication authentication,
            @PathVariable Long jobId) {
        Long userId = (Long) authentication.getPrincipal();
        translationService.getJobStatus(userId, jobId);

        SseEmitter emitter = new SseEmitter(300_000L);

        String channelName = translationService.getChannelName(jobId);
        ChannelTopic topic = new ChannelTopic(channelName);

        MessageListener listener = (message, pattern) -> {
            try {
                String body = new String(message.getBody());
                emitter.send(SseEmitter.event()
                        .data(body, MediaType.APPLICATION_JSON));

                if (body.contains("\"event\":\"translation-complete\"")
                        || body.contains("\"event\":\"translation-error\"")) {
                    emitter.complete();
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        };

        listenerContainer.addMessageListener(listener, topic);

        emitter.onCompletion(() -> listenerContainer.removeMessageListener(listener, topic));
        emitter.onTimeout(() -> {
            listenerContainer.removeMessageListener(listener, topic);
            emitter.complete();
        });
        emitter.onError(e -> listenerContainer.removeMessageListener(listener, topic));

        return emitter;
    }

    @GetMapping("/api/translations/{jobId}")
    public ResponseEntity<ApiResponse<TranslationJobResponse>> getTranslationStatus(
            Authentication authentication,
            @PathVariable Long jobId) {
        Long userId = (Long) authentication.getPrincipal();
        TranslationJobResponse response = translationService.getJobStatus(userId, jobId);
        return ResponseEntity.ok(ApiResponse.ok("번역 상태를 조회했습니다.", response));
    }
}
