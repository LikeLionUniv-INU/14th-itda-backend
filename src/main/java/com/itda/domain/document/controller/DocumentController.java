package com.itda.domain.document.controller;

import com.itda.domain.document.dto.request.CreateDocumentRequest;
import com.itda.domain.document.dto.request.CreateVersionRequest;
import com.itda.domain.document.dto.request.SaveDocumentRequest;
import com.itda.domain.document.dto.response.ChangeSummaryResponse;
import com.itda.domain.document.dto.response.CreateDocumentResponse;
import com.itda.domain.document.dto.response.DocumentDetailResponse;
import com.itda.domain.document.dto.response.DocumentVersionResponse;
import com.itda.domain.document.service.ChangeTrackingService;
import com.itda.domain.document.service.DocumentService;
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

@Tag(name = "문서 관리", description = "문서 생성/조회/저장/버전 관리")
@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final ChangeTrackingService changeTrackingService;

    @Operation(summary = "문서 생성", description = "팀장이 새 문서를 생성합니다.")
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

    @Operation(summary = "문서 상세 조회", description = "버전별 문서를 조회합니다. lang 파라미터로 번역된 문서를 조회할 수 있습니다.")
    @GetMapping("/api/documents/{documentId}/versions/{version}")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getDocumentVersion(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @RequestParam(required = false) String lang) {
        Long userId = (Long) authentication.getPrincipal();
        DocumentDetailResponse response = documentService.getDocumentVersion(userId, documentId, version, lang);
        return ResponseEntity.ok(ApiResponse.ok("문서를 조회했습니다.", response));
    }

    @Operation(summary = "버전 목록 조회", description = "문서의 전체 버전 이력을 조회합니다.")
    @GetMapping("/api/documents/{documentId}/versions")
    public ResponseEntity<ApiResponse<List<DocumentVersionResponse>>> getVersionList(
            Authentication authentication,
            @PathVariable Long documentId) {
        Long userId = (Long) authentication.getPrincipal();
        List<DocumentVersionResponse> response = documentService.getVersionList(userId, documentId);
        return ResponseEntity.ok(ApiResponse.ok("버전 목록을 조회했습니다.", response));
    }

    @Operation(summary = "문서 전체 저장", description = "페이지/핀/요구사항을 포함한 문서 전체를 저장합니다.")
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

    @Operation(summary = "문서 임시저장", description = "작업 중인 문서를 임시 저장합니다.")
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

    @Operation(summary = "새 버전 생성", description = "기준 버전을 복사하여 새 버전을 생성합니다.")
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

    @Operation(summary = "버전 삭제", description = "해당 버전을 삭제합니다. 마지막 버전은 삭제할 수 없습니다.")
    @DeleteMapping("/api/documents/{documentId}/versions/{version}")
    public ResponseEntity<ApiResponse<Void>> deleteVersion(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version) {
        Long userId = (Long) authentication.getPrincipal();
        documentService.deleteVersion(userId, documentId, version);
        return ResponseEntity.ok(ApiResponse.ok("버전이 삭제되었습니다.", null));
    }

    @Operation(summary = "수정사항 목록 조회", description = "해당 버전의 수정사항 요약과 확인 상태를 조회합니다. lang 파라미터로 번역된 내용을 조회할 수 있습니다.")
    @GetMapping("/api/documents/{documentId}/versions/{version}/changes")
    public ResponseEntity<ApiResponse<ChangeSummaryResponse>> getChanges(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @RequestParam(required = false) String lang) {
        Long userId = (Long) authentication.getPrincipal();
        Long versionId = documentService.getDocumentVersionId(userId, documentId, version);
        ChangeSummaryResponse response = changeTrackingService.getChangeSummary(versionId, userId, lang);
        return ResponseEntity.ok(ApiResponse.ok("수정사항을 조회했습니다.", response));
    }

    @Operation(summary = "수정사항 확인", description = "개별 수정사항을 확인 처리합니다.")
    @PostMapping("/api/documents/{documentId}/versions/{version}/changes/{changeId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmChange(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version,
            @PathVariable Long changeId) {
        Long userId = (Long) authentication.getPrincipal();
        changeTrackingService.confirmChange(changeId, userId);
        return ResponseEntity.ok(ApiResponse.ok("수정사항을 확인했습니다.", null));
    }

    @Operation(summary = "수정사항 전체 확인", description = "해당 버전의 모든 수정사항을 확인 처리합니다.")
    @PostMapping("/api/documents/{documentId}/versions/{version}/changes/confirm-all")
    public ResponseEntity<ApiResponse<Void>> confirmAllChanges(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable Integer version) {
        Long userId = (Long) authentication.getPrincipal();
        Long versionId = documentService.getDocumentVersionId(userId, documentId, version);
        changeTrackingService.confirmAllChanges(versionId, userId);
        return ResponseEntity.ok(ApiResponse.ok("모든 수정사항을 확인했습니다.", null));
    }
}
