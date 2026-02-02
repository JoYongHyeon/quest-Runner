package com.questrunner.questrunner.global.security.oauth2;

import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.member.vo.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record CustomOAuth2User(
        Long memberId,
        String email,
        UserRole role,
        Map<String, Object> attributes
) implements OAuth2User {

    public static CustomOAuth2User from(MemberEntity member, Map<String, Object> attributes) {
        return new CustomOAuth2User(
                member.getId(),
                member.getEmail(),
                member.getRole(),
                attributes
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_USER, ROLE_ADMIN 등 권한 (String 으로 변환)
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * OAuth2User 인터페이스 상 getName() 은 String 을 반환해야만 한다.
     */
    @Override
    public String getName() {
        return String.valueOf(memberId);
    }
}
