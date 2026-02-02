package com.questrunner.questrunner.global.config;


import org.hibernate.annotations.Comment;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "jwt")
@Comment("jwt.* 설정 바인딩")
public record JwtProperties(
        String secret,
        long accessTokenExpiration,
        long refreshTokenExpiration
) {}
