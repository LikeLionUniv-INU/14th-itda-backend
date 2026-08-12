package com.itda.domain.page.controller;

import com.itda.domain.page.dto.request.CreatePageRequest;
import com.itda.domain.page.dto.request.ReorderPagesRequest;
import com.itda.domain.page.dto.request.UpdatePageRequest;
import com.itda.domain.page.dto.response.PageResponse;
import com.itda.domain.page.service.PageService;
import com.itda.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "페이지", description = "페이지 추가/수정/삭제/순서변경")
@RestController
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @Operation(summary = "페이지 추가", description = "문서 버전에 새 페이지를 추가합니다.")
    @PostMapping("/api/documents/{documentId}/versions/{version}/pages")
    public ResponseEntity<ApiResponse<PageResponse>> createPage(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @Valid @RequestBody CreatePageRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PageResponse response = pageService.createPage(userId, documentId, version, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("페이지가 추가되었습니다.", response));
    }

    @Operation(summary = "페이지 수정", description = "페이지의 화면 이름/ID를 수정합니다.")
    @PutMapping("/api/documents/{documentId}/versions/{version}/pages/{pageId}")
    public ResponseEntity<ApiResponse<PageResponse>> updatePage(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @PathVariable Long pageId,
            @Valid @RequestBody UpdatePageRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PageResponse response = pageService.updatePage(userId, pageId, request);
        return ResponseEntity.ok(ApiResponse.ok("페이지가 수정되었습니다.", response));
    }

    @Operation(summary = "페이지 삭제", description = "페이지를 삭제합니다. 와이어프레임/핀/요구사항도 함께 삭제됩니다.")
    @DeleteMapping("/api/documents/{documentId}/versions/{version}/pages/{pageId}")
    public ResponseEntity<ApiResponse<Void>> deletePage(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @PathVariable Long pageId) {
        Long userId = (Long) authentication.getPrincipal();
        pageService.deletePage(userId, pageId);
        return ResponseEntity.ok(ApiResponse.ok("페이지가 삭제되었습니다.", null));
    }

    @Operation(summary = "페이지 순서 변경", description = "페이지 순서를 변경합니다.")
    @PatchMapping("/api/documents/{documentId}/versions/{version}/pages/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderPages(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @Valid @RequestBody ReorderPagesRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        pageService.reorderPages(userId, documentId, version, request);
        return ResponseEntity.ok(ApiResponse.ok("페이지 순서가 변경되었습니다.", null));
    }
}
