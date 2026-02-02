package com.questrunner.questrunner.global.security.jwt;

import com.questrunner.questrunner.global.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
@RequiredArgsConstructor
@Comment("토큰 생성/검증")
public class JwtProvider {

    private final JwtProperties props;
    private SecretKey key;

    // yaml 의 평문 secret 을 HMAC-SHA 알고리즘용 키 객체로 변환 및 캐싱
    private SecretKey getKey() {
        if (key == null) {
            key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        }
        return key;
    }

    // [Access Token] 단기 인증용: 유저 ID, 권한(Role)을 포함
    public String createAccessToken(Long id, String role) {
        long now = System.currentTimeMillis();
        Date exp = new Date(now + props.accessTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(id)) // 유저 식별자(PK)
                .claim("role", role)   // 커스텀 클레임: 권한
                .expiration(exp)             // 만료 시간 설정
                .signWith(getKey())          // 암호화 서명
                .compact();
    }

    // [Refresh Token] 재발급용: 보안을 위해 최소한의 정보(ID)만 포함
    public String createRefreshToken(Long id) {
        long now = System.currentTimeMillis();
        Date exp = new Date(now + props.refreshTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(id))
                .expiration(exp)
                .signWith(getKey())
                .compact();
    }

    // 토큰에서 유저 고유 ID(Subject) 추출
    public Long getUserId(String token) {
        return Long.parseLong(
                Jwts.parser().verifyWith(getKey()).build()
                        .parseSignedClaims(token)
                        .getPayload().getSubject()
        );
    }

    // 토큰에서 유저 권한(Role) 추출 - DB 조회 없이 인가 처리를 가능하게 함
    public String getUserRole(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    // 토큰에서 유저 이메일 추출
    public String getUserEmail(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    // 토큰 유형성 검증 (서명 변조, 만료 여부 확인)
    public boolean validate(String token) {
        Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token);  // 내부적으로 만료 시 예외를 던짐
        return true;
    }
}
