package com.questrunner.questrunner.global.security.handler;

import com.questrunner.questrunner.global.config.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Comment("실패 핸들러 - JWT 발급 + 리다이렉트")
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2Properties props;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest req,
            HttpServletResponse res,
            AuthenticationException ex
    ) throws IOException {


        // 실패 이유를 쿼리 파라미터로 붙여 프론트로 전달
        String redirect = UriComponentsBuilder
                .fromUriString(props.redirectUri())
                .queryParam("error", ex.getLocalizedMessage())
                .encode()
                .build().
                toUriString();

        getRedirectStrategy().sendRedirect(req, res, redirect);
    }
}
