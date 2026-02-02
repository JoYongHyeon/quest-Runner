package com.questrunner.questrunner.global.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        try {
            // 다음 필터(JwtAuthenticationFilter) 실행
            chain.doFilter(req, res);

        } catch (ExpiredJwtException e) {
            setErrorResponse(res, "TOKEN_EXPIRED");
        } catch (JwtException | IllegalArgumentException e) {
            setErrorResponse(res, "INVALID_TOKEN");
        }
    }

    /**
     * 프론트엔드와 약속된 인증 에러 응답 규칙 규격
     * - TOKEN_EXPIRED: 프론트에서 401 감지 시 /api/auth/reissue(재발급) 호출 유도
     * - INVALID_TOKEN: 로그인 페이지로 리다이렉트 권장
     */
    private void setErrorResponse(HttpServletResponse res, String code) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"code\":\"" + code + "\"}");
    }
}
