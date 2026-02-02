package com.questrunner.questrunner.global.security.handler;

import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.questrunner.questrunner.application.auth.AuthService;
import com.questrunner.questrunner.global.config.OAuth2Properties;
import com.questrunner.questrunner.global.security.oauth2.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Comment("성공 핸들러 - JWT 발급 및 리다이렉트")
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuth2Properties props;


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest req,
            HttpServletResponse res,
            Authentication auth) throws IOException {


        // 1. 사용자 정보 추출
        CustomOAuth2User principal = (CustomOAuth2User) auth.getPrincipal();
        Long userId = principal.memberId();
        String role = principal.role().getAuthority();

        // 2. AuthService 토큰 발급 요청
        String[] tokens = authService.issueTokens(userId, role);
        String accessToken  = tokens[0];
        String refreshToken = tokens[1];

        // 3. 응답 처리 (쿠키 & 리다이렉트)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
//                .secure(true) // TODO: 실서비스에서는 true, 로컬 HTTP 면 false 고려
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());


        String redirect = UriComponentsBuilder.fromUriString(props.redirectUri())
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        // 4. front 최종 이동
        getRedirectStrategy().sendRedirect(req, res, redirect);
    }
}
