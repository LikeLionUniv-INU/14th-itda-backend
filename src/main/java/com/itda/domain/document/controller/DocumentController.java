package com.itda.domain.document.controller;

import com.itda.domain.document.dto.request.CreateDocumentRequest;
import com.itda.domain.document.dto.request.CreateVersionRequest;
import com.itda.domain.document.dto.request.SaveDocumentRequest;
import com.itda.domain.document.dto.response.CreateDocumentResponse;
import com.itda.domain.document.dto.response.DocumentDetailResponse;
import com.itda.domain.document.dto.response.DocumentVersionResponse;
import com.itda.domain.document.service.DocumentService;
import com.itda.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/api/teams/{teamId}/documents")
    public ResponseEntity<ApiResponse<CreateDocumentResponse>> createDocument(
            Authentication authentication,
            @PathVariable Long teamId,
            @Valid @RequestBody CreateDocumentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CreateDocumentResponse response = documentService.createDocument(userId, teamId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("문서가 생성되었습니다.", response));
    }

    @GetMapping("/api/documents/{documentId}/versions/{version}")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getDocumentVersion(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version) {
        Long userId = (Long) authentication.getPrincipal();
        DocumentDetailResponse response = documentService.getDocumentVersion(userId, documentId, version);
        return ResponseEntity.ok(ApiResponse.ok("문서를 조회했습니다.", response));
    }

    @GetMapping("/api/documents/{documentId}/versions")
    public ResponseEntity<ApiResponse<List<DocumentVersionResponse>>> getVersionList(
            Authentication authentication,
            @PathVariable Long documentId) {
        Long userId = (Long) authentication.getPrincipal();
        List<DocumentVersionResponse> response = documentService.getVersionList(userId, documentId);
        return ResponseEntity.ok(ApiResponse.ok("버전 목록을 조회했습니다.", response));
    }

    @PutMapping("/api/documents/{documentId}/versions/{version}")
    public ResponseEntity<ApiResponse<Void>> saveDocument(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @RequestBody SaveDocumentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        documentService.saveDocument(userId, documentId, version, request);
        return ResponseEntity.ok(ApiResponse.ok("문서가 저장되었습니다.", null));
    }

    @PostMapping("/api/documents/{documentId}/versions/{version}/auto-save")
    public ResponseEntity<ApiResponse<Void>> autoSaveDocument(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @RequestBody SaveDocumentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        documentService.autoSaveDocument(userId, documentId, version, request);
        return ResponseEntity.ok(ApiResponse.ok("임시 저장되었습니다.", null));
    }

    @PostMapping("/api/documents/{documentId}/versions")
    public ResponseEntity<ApiResponse<DocumentVersionResponse>> createNewVersion(
            Authentication authentication,
            @PathVariable Long documentId,
            @Valid @RequestBody CreateVersionRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        DocumentVersionResponse response = documentService.createNewVersion(userId, documentId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("새 버전이 생성되었습니다.", response));
    }

    @DeleteMapping("/api/documents/{documentId}/versions/{version}")
    public ResponseEntity<ApiResponse<Void>> deleteVersion(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version) {
        Long userId = (Long) authentication.getPrincipal();
        documentService.deleteVersion(userId, documentId, version);
        return ResponseEntity.ok(ApiResponse.ok("버전이 삭제되었습니다.", null));
    }
}
