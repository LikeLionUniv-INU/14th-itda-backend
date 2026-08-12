package com.itda.domain.requirement.controller;

import com.itda.domain.requirement.dto.request.CreateRequirementRequest;
import com.itda.domain.requirement.dto.request.UpdateRequirementRequest;
import com.itda.domain.requirement.dto.response.RequirementResponse;
import com.itda.domain.requirement.service.RequirementService;
import com.itda.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "요구사항", description = "요구사항 추가/수정/삭제")
@RestController
@RequestMapping("/api/pins/{pinId}/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @Operation(summary = "요구사항 추가", description = "핀에 요구사항을 추가합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<RequirementResponse>> createRequirement(
            Authentication authentication,
            @PathVariable Long pinId,
            @Valid @RequestBody CreateRequirementRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        RequirementResponse response = requirementService.createRequirement(userId, pinId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("요구사항이 추가되었습니다.", response));
    }

    @Operation(summary = "요구사항 수정", description = "요구사항을 수정합니다. 빈 content는 허용되지 않습니다.")
    @PutMapping("/{requirementId}")
    public ResponseEntity<ApiResponse<RequirementResponse>> updateRequirement(
            Authentication authentication,
            @PathVariable Long pinId,
            @PathVariable Long requirementId,
            @Valid @RequestBody UpdateRequirementRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        RequirementResponse response = requirementService.updateRequirement(userId, pinId, requirementId, request);
        return ResponseEntity.ok(ApiResponse.ok("요구사항이 수정되었습니다.", response));
    }

    @Operation(summary = "요구사항 삭제", description = "요구사항을 삭제합니다.")
    @DeleteMapping("/{requirementId}")
    public ResponseEntity<ApiResponse<Void>> deleteRequirement(
            Authentication authentication,
            @PathVariable Long pinId,
            @PathVariable Long requirementId) {
        Long userId = (Long) authentication.getPrincipal();
        requirementService.deleteRequirement(userId, pinId, requirementId);
        return ResponseEntity.ok(ApiResponse.ok("요구사항이 삭제되었습니다.", null));
    }
}
