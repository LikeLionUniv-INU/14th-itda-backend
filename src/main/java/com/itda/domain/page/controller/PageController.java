package com.itda.domain.page.controller;

import com.itda.domain.page.dto.request.CreatePageRequest;
import com.itda.domain.page.dto.request.ReorderPagesRequest;
import com.itda.domain.page.dto.request.UpdatePageRequest;
import com.itda.domain.page.dto.response.PageResponse;
import com.itda.domain.page.service.PageService;
import com.itda.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

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
