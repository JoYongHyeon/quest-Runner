package com.questrunner.questrunner.global.config;

import org.hibernate.annotations.Comment;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.oauth2")
@Comment("app.oauth2.* 설정 바인딩")
public record OAuth2Properties(
        // OAuth2 로그인 성공/실패 후 프론트로 redirect 할 주소
        String redirectUri
) {}
