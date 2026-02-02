package com.questrunner.questrunner.global.security.oauth2;

import java.util.Map;

public class OAuth2UserInfoFactory {

    // provider(google, naver, kakao 등) 에 따라 적절한 UserInfo 구현체 생성
    public static OAuth2UserInfo create(String provider, Map<String, Object> attributes) {

        if ("google".equals(provider)) {
            return new GoogleOAuth2UserInfo(attributes);
        }

        throw new IllegalArgumentException("Unsupported OAuth Provider: " + provider);
    }}
