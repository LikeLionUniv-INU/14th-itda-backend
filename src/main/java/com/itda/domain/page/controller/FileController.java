package com.itda.domain.page.controller;

import com.itda.domain.page.dto.request.CreateWireframeImageRequest;
import com.itda.domain.page.dto.request.PresignedUrlRequest;
import com.itda.domain.page.dto.request.UpdateWireframeImageRequest;
import com.itda.domain.page.dto.response.PresignedUrlResponse;
import com.itda.domain.page.dto.response.WireframeImageResponse;
import com.itda.domain.page.service.WireframeImageService;
import com.itda.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final WireframeImageService wireframeImageService;

    @PostMapping("/api/files/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            Authentication authentication,
            @Valid @RequestBody PresignedUrlRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PresignedUrlResponse response = wireframeImageService.generatePresignedUrl(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Presigned URL이 발급되었습니다.", response));
    }

    @PostMapping("/api/pages/{pageId}/wireframe-images")
    public ResponseEntity<ApiResponse<WireframeImageResponse>> createWireframeImage(
            Authentication authentication,
            @PathVariable Long pageId,
            @Valid @RequestBody CreateWireframeImageRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        WireframeImageResponse response = wireframeImageService.createWireframeImage(userId, pageId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("이미지가 등록되었습니다.", response));
    }

    @PutMapping("/api/pages/{pageId}/wireframe-images/{imageId}")
    public ResponseEntity<ApiResponse<WireframeImageResponse>> updateWireframeImage(
            Authentication authentication,
            @PathVariable Long pageId,
            @PathVariable Long imageId,
            @Valid @RequestBody UpdateWireframeImageRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        WireframeImageResponse response = wireframeImageService.updateWireframeImage(userId, pageId, imageId, request);
        return ResponseEntity.ok(ApiResponse.ok("이미지가 수정되었습니다.", response));
    }

    @DeleteMapping("/api/pages/{pageId}/wireframe-images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteWireframeImage(
            Authentication authentication,
            @PathVariable Long pageId,
            @PathVariable Long imageId) {
        Long userId = (Long) authentication.getPrincipal();
        wireframeImageService.deleteWireframeImage(userId, pageId, imageId);
        return ResponseEntity.ok(ApiResponse.ok("이미지가 삭제되었습니다.", null));
    }
}
