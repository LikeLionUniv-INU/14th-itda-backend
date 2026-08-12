package com.itda.domain.auth.service;

import com.itda.domain.auth.dto.request.LoginRequest;
import com.itda.domain.auth.dto.request.RefreshRequest;
import com.itda.domain.auth.dto.request.SignupRequest;
import com.itda.domain.auth.dto.response.SignupResponse;
import com.itda.domain.auth.dto.response.TokenResponse;
import com.itda.domain.auth.entity.RefreshToken;
import com.itda.domain.auth.repository.RefreshTokenRepository;
import com.itda.domain.user.entity.User;
import com.itda.domain.user.repository.UserRepository;
import com.itda.global.error.DuplicateException;
import com.itda.global.error.UnauthorizedException;
import com.itda.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateException("해당 아이디는 사용할 수 없습니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .country(request.country())
                .language(request.language())
                .build();

        userRepository.save(user);
        return SignupResponse.from(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("알맞은 비밀번호를 입력해주세요."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("알맞은 비밀번호를 입력해주세요.");
        }

        return createTokens(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            throw new UnauthorizedException("로그인이 만료되었습니다. 다시 로그인해주세요.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("로그인이 만료되었습니다. 다시 로그인해주세요."));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("로그인이 만료되었습니다. 다시 로그인해주세요.");
        }

        User user = refreshToken.getUser();

        // Rotation: 기존 토큰 삭제 후 새 토큰 발급
        refreshTokenRepository.delete(refreshToken);
        return createTokens(user);
    }

    private TokenResponse createTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 기존 refresh token 삭제
        refreshTokenRepository.deleteByUser_Id(user.getId());

        // 새 refresh token 저장
        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtTokenProvider.getRefreshTokenExpiryMillis() / 1000))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return TokenResponse.of(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpirySeconds());
    }
}
