package com.itda.domain.pin.controller;

import com.itda.domain.pin.dto.request.CreatePinRequest;
import com.itda.domain.pin.dto.request.UpdatePinRequest;
import com.itda.domain.pin.dto.response.PinDetailResponse;
import com.itda.domain.pin.dto.response.PinResponse;
import com.itda.domain.pin.service.PinService;
import com.itda.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "핀", description = "핀 추가/수정/삭제/조회")
@RestController
@RequestMapping("/api/pages/{pageId}/pins")
@RequiredArgsConstructor
public class PinController {

    private final PinService pinService;

    @Operation(summary = "핀 추가", description = "페이지에 핀을 추가합니다. 핀 번호는 자동 할당됩니다.")
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

    @Operation(summary = "핀 위치 수정", description = "핀의 x, y 좌표를 수정합니다.")
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

    @Operation(summary = "핀 삭제", description = "핀을 삭제합니다. 연결된 요구사항도 함께 삭제됩니다.")
    @DeleteMapping("/{pinId}")
    public ResponseEntity<ApiResponse<Void>> deletePin(
            Authentication authentication,
            @PathVariable Long pageId,
            @PathVariable Long pinId) {
        Long userId = (Long) authentication.getPrincipal();
        pinService.deletePin(userId, pageId, pinId);
        return ResponseEntity.ok(ApiResponse.ok("핀이 삭제되었습니다.", null));
    }

    @Operation(summary = "핀 목록 조회", description = "페이지의 핀 목록을 요구사항과 함께 조회합니다. tabType으로 필터링 가능합니다.")
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
