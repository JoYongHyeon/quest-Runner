package com.questrunner.questrunner.api.auth;

import com.questrunner.questrunner.api.auth.dto.res.TokenResDTO;
import com.questrunner.questrunner.application.auth.AuthService;
import com.questrunner.questrunner.application.member.MemberService;
import com.questrunner.questrunner.global.enums.ErrorCode;
import com.questrunner.questrunner.global.enums.SuccessCode;
import com.questrunner.questrunner.global.exception.BusinessException;
import com.questrunner.questrunner.global.security.oauth2.CustomOAuth2User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/refresh")
    public ResponseEntity<TokenResDTO> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {

        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_REFRESH_TOKEN);
        }

        String[] tokens = authService.refresh(refreshToken);

        // 쿠키 재설정
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens[1])
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                // 가장 엄격 ( 같은 사이트에서 온 요청에만 쿠키를 보냄
                .sameSite("Strict")
                // Lax 권장 -> 기본값
//                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new TokenResDTO(tokens[0]));

    }
}


