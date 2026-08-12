package com.itda.domain.pin.controller;

import com.itda.domain.pin.dto.request.CreatePinRequest;
import com.itda.domain.pin.dto.request.UpdatePinRequest;
import com.itda.domain.pin.dto.response.PinDetailResponse;
import com.itda.domain.pin.dto.response.PinResponse;
import com.itda.domain.pin.service.PinService;
import com.itda.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages/{pageId}/pins")
@RequiredArgsConstructor
public class PinController {

    private final PinService pinService;

    @PostMapping
    public ResponseEntity<ApiResponse<PinResponse>> createPin(
            Authentication authentication,
            @PathVariable Long pageId,
            @Valid @RequestBody CreatePinRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PinResponse response = pinService.createPin(userId, pageId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("핀이 추가되었습니다.", response));
    }

    @PutMapping("/{pinId}")
    public ResponseEntity<ApiResponse<PinResponse>> updatePin(
            Authentication authentication,
            @PathVariable Long pageId,
            @PathVariable Long pinId,
            @Valid @RequestBody UpdatePinRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PinResponse response = pinService.updatePin(userId, pageId, pinId, request);
        return ResponseEntity.ok(ApiResponse.ok("핀 위치가 수정되었습니다.", response));
    }

    @DeleteMapping("/{pinId}")
    public ResponseEntity<ApiResponse<Void>> deletePin(
            Authentication authentication,
            @PathVariable Long pageId,
            @PathVariable Long pinId) {
        Long userId = (Long) authentication.getPrincipal();
        pinService.deletePin(userId, pageId, pinId);
        return ResponseEntity.ok(ApiResponse.ok("핀이 삭제되었습니다.", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PinDetailResponse>>> getPins(
            Authentication authentication,
            @PathVariable Long pageId,
            @RequestParam(required = false) String tabType) {
        Long userId = (Long) authentication.getPrincipal();
        List<PinDetailResponse> response = pinService.getPins(userId, pageId, tabType);
        return ResponseEntity.ok(ApiResponse.ok("핀 목록을 조회했습니다.", response));
    }
}
