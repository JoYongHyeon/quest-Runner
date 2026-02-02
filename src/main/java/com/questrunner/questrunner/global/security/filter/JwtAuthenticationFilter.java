package com.questrunner.questrunner.global.security.filter;

import com.questrunner.questrunner.domain.member.vo.UserRole;
import com.questrunner.questrunner.global.security.jwt.JwtProvider;
import com.questrunner.questrunner.global.security.oauth2.CustomOAuth2User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Comment("매 요청마다 JWT 검사")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull FilterChain chain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 JWT 추출
        String jwt = resolveToken(req);

            // 2. 유효하면 인증 처리, 유효하지 않으면 (예외 발생 시) 그냥 위로 던짐
            if (StringUtils.hasText(jwt) && jwtProvider.validate(jwt)) {
                Long userId  = jwtProvider.getUserId(jwt);
                String email = jwtProvider.getUserEmail(jwt);
                String role  = jwtProvider.getUserRole(jwt);

                // 3. 토큰 정보를 바탕으로 CustomOAuth2User 객체 복원
                CustomOAuth2User principal = new CustomOAuth2User(
                        userId,
                        email,
                        UserRole.valueOf(role.replace("ROLE_", "")),
                        null
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        chain.doFilter(req, res);
    }


    // "Bearer xxx" 형태에서 실제 토큰 부분만 추출
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
