package com.itda.domain.team.controller;

import com.itda.domain.team.dto.request.CreateTeamRequest;
import com.itda.domain.team.dto.request.JoinTeamRequest;
import com.itda.domain.team.dto.response.CreateTeamResponse;
import com.itda.domain.team.dto.response.InviteCodeResponse;
import com.itda.domain.team.dto.response.JoinTeamResponse;
import com.itda.domain.team.dto.response.TeamDetailResponse;
import com.itda.domain.team.service.TeamService;
import com.itda.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "팀 프로젝트", description = "팀 생성/참여/조회/초대코드")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "팀 프로젝트 생성", description = "새 팀 프로젝트를 생성하고 초대 코드를 발급합니다. 생성자가 팀장이 됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateTeamResponse>> createTeam(
            Authentication authentication,
            @Valid @RequestBody CreateTeamRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CreateTeamResponse response = teamService.createTeam(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("팀 프로젝트가 생성되었습니다.", response));
    }

    @Operation(summary = "팀 프로젝트 참여", description = "초대 코드로 팀 프로젝트에 참여합니다.")
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<JoinTeamResponse>> joinTeam(
            Authentication authentication,
            @Valid @RequestBody JoinTeamRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        JoinTeamResponse response = teamService.joinTeam(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("팀 프로젝트에 참여했습니다.", response));
    }

    @Operation(summary = "팀 프로젝트 상세 조회", description = "팀 정보, 멤버 목록, 문서 목록, 활동 요약을 조회합니다.")
    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> getTeamDetail(
            Authentication authentication,
            @PathVariable Long teamId) {
        Long userId = (Long) authentication.getPrincipal();
        TeamDetailResponse response = teamService.getTeamDetail(userId, teamId);
        return ResponseEntity.ok(ApiResponse.ok("팀 프로젝트 정보를 조회했습니다.", response));
    }

    @Operation(summary = "초대 코드 조회", description = "팀 프로젝트의 초대 코드를 조회합니다.")
    @GetMapping("/{teamId}/invite-code")
    public ResponseEntity<ApiResponse<InviteCodeResponse>> getInviteCode(
            Authentication authentication,
            @PathVariable Long teamId) {
        Long userId = (Long) authentication.getPrincipal();
        InviteCodeResponse response = teamService.getInviteCode(userId, teamId);
        return ResponseEntity.ok(ApiResponse.ok("초대 코드를 조회했습니다.", response));
    }
}
