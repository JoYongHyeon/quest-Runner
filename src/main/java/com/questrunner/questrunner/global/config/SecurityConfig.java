package com.questrunner.questrunner.global.config;

import com.questrunner.questrunner.global.security.filter.JwtAuthenticationFilter;
import com.questrunner.questrunner.global.security.filter.JwtExceptionFilter;
import com.questrunner.questrunner.global.security.handler.OAuth2AuthenticationFailureHandler;
import com.questrunner.questrunner.global.security.handler.OAuth2AuthenticationSuccessHandler;
import com.questrunner.questrunner.global.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                /*
                 * CSRF 비활성화
                 * REST API + JWT 조합에서는 서버 세션을 사용하지 않기 때문에 CSRF 보호 필요 없음.
                 */
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 대신 JWT 사용 -> STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /*
                 * CORS 설정 적용
                 * - 프론트에서 오는 요청 허용 규칙
                 */
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                /*
                 * H2 콘솔 사용을 위해 프레임 옵션 비활성화
                 * - 개발 환경에서만 사용
                 */
                .headers(headers -> headers
                        // H2 콘솔을 위해 완전히 비활성화
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                /*
                 * URL 인가(Authorization) 규칙
                 *
                 * - permitAll() 된 URL:
                 *   - JwtAuthenticationFilter는 무조건 실행되지만,
                 *     인증이 필요하지 않므로 SecurityContext에 인증이 없어도 통과됨.
                 *
                 * - authenticated():
                 *   - JwtAuthenticationFilter.java 가 JWT를 검증해 SecurityContext에 인증을 세팅해야만 접근 가능.
                 *   - 인증 정보가 없으면 401 발생.
                 */
                .authorizeHttpRequests(auth -> auth

                        // 로그인 관련, 헬스체크 등은 모두 허용
                        .requestMatchers(
                                "/error",
                                "/favicon.ico",
                                "/oauth2/**",     // OAuth2 Authorization 요청 및 콜백 경로
                                "/h2-console/**", // H2 콘솔 (개발용)

                                // TODO: 화면 없이 임시 화면용 나중에 뺴야함
                                "/*.html",
                                "/*.css",
                                "/*.js"
                        ).permitAll()

                        // 결제 승인, 포인트 차감 등 실제 돈이 오가는 API 는 무조건 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                /*
                 * OAuth2 로그인 설정
                 *
                 * - 사용자가 /oauth2/authorization/google 을 호출하면
                 *   Spring Security 내부 필터가 로그인 흐름을 자동 처리.
                 *
                 * - 로그인 성공 흐름:
                 *    Google Redirect → OAuth2LoginAuthenticationFilter → CustomOAuth2UserService.loadUser()
                 *    → OAuth2AuthenticationSuccessHandler → JWT 발급
                 *
                 * - 로그인 실패도 failureHandler에서 처리.
                 */
                .oauth2Login(oauth -> oauth
                        // 로그인 버튼이 있는 커스텀 페이지
                        .loginPage("/login.html")
                        .userInfoEndpoint(info -> info.userService(customOAuth2UserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )

                /*
                 * JWT 인증 필터 등록
                 *
                 * JwtAuthenticationFilter 특징:
                 * - Spring Security FilterChain 에 들어오는 모든 요청에서 실행됨.
                 * - Authorization: Bearer <JWT> 가 있으면
                 *     → JWT 검증
                 *     → userId 추출
                 *     → DB 조회해 MemberEntity 를 Authentication 에 세팅
                 *
                 * - JWT가 없거나 유효하지 않으면 인증 세팅 없이 그냥 다음 필터로 넘김.
                 *
                 * 즉, 로그인 이후 API 인증은 이 필터가 전담함.
                 */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
