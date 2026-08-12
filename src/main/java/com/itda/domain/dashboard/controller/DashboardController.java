package com.itda.domain.dashboard.controller;

import com.itda.domain.dashboard.dto.response.DashboardResponse;
import com.itda.domain.dashboard.dto.response.DocumentListResponse;
import com.itda.domain.dashboard.dto.response.ProjectListResponse;
import com.itda.domain.dashboard.service.DashboardService;
import com.itda.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        DashboardResponse response = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(ApiResponse.ok("대시보드 정보를 조회했습니다.", response));
    }

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<ProjectListResponse>> getProjects(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ProjectListResponse response = dashboardService.getProjects(userId);
        return ResponseEntity.ok(ApiResponse.ok("프로젝트 목록을 조회했습니다.", response));
    }

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<DocumentListResponse>> getDocuments(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        DocumentListResponse response = dashboardService.getDocuments(userId);
        return ResponseEntity.ok(ApiResponse.ok("문서 목록을 조회했습니다.", response));
    }
}
