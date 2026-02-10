package com.questrunner.questrunner.application.auth;

import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.member.repository.MemberRepository;
import com.questrunner.questrunner.global.enums.ErrorCode;
import com.questrunner.questrunner.global.exception.BusinessException;
import com.questrunner.questrunner.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    /**
     * [토큰 발급 로직]
     * - 최초 로그인 시 Access/Refresh 토큰 쌍을 발급
     * @param userId 사용자 PK
     * @param role role 사용자 권한
     * @return [0]: AccessToken, [1]: RefreshToken
     */
    @Transactional
    public String[] issueTokens(Long userId, String role) {
        String accessToken   = jwtProvider.createAccessToken(userId, role);
        String refreshToken  = jwtProvider.createRefreshToken(userId);

        return new String[]{accessToken, refreshToken};
    }

    /**
     * [토큰 갱신 로직]
     * - Refresh Token 을 검증하고 새로운 토큰 쌍을 발급
     */
    @Transactional
    public String[] refresh(String refreshToken) {

        // 1. 토큰 존재 여부
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_REFRESH_TOKEN);
        }

        // 2. 토큰 유효성 검증
        if (!jwtProvider.validate(refreshToken)) {
            // 만료 또는 위조
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 3. 사용자 ID 추출
        Long userId = jwtProvider.getUserId(refreshToken);

        // 4. refreshToken 에는 Role 이 없으므로, DB 에서 최신 Role 을 조회해야 함.
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String role = member.getRole().getAuthority();

        // 5. 새 토큰 발급
        return issueTokens(userId, role);
    }
}
