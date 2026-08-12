package com.itda.domain.user.controller;

import com.itda.domain.user.dto.response.UserResponse;
import com.itda.domain.user.service.UserService;
import com.itda.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자", description = "사용자 정보 조회")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse response = userService.getMyInfo(userId);
        return ResponseEntity.ok(ApiResponse.ok("사용자 정보를 조회했습니다.", response));
    }
}
