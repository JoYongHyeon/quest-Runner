package com.questrunner.questrunner.global.security.service;

import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.member.repository.MemberRepository;
import com.questrunner.questrunner.domain.member.vo.UserRole;
import com.questrunner.questrunner.domain.member.vo.UserStatus;
import com.questrunner.questrunner.global.security.oauth2.CustomOAuth2User;
import com.questrunner.questrunner.global.security.oauth2.OAuth2UserInfo;
import com.questrunner.questrunner.global.security.oauth2.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Comment("OAuth2 로그인 시 회원 조회/등록 처리")
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest req) {

        // 1. 기본 구현으로부터 OAuth2User 가져오기 (구글 응답 값)
        OAuth2User oAuth2User = super.loadUser(req);

        // 2. 어떤 provider(google, naver...) 인지
        String provider = req.getClientRegistration().getRegistrationId();

        // 3. provider 에 맞는 UserInfo 구현체 생성
        OAuth2UserInfo userInfo =
                OAuth2UserInfoFactory.create(provider, oAuth2User.getAttributes());

        // 4. 회원 조회 또는 등록
        Optional<MemberEntity> memberOptional = memberRepository.findByProviderAndProviderId(provider, userInfo.getId());

        MemberEntity member;
        if (memberOptional.isPresent()) {
            // [기존 회원]: 마지막 로그인 시간만 갱신
            member = memberOptional.get();
            member.completeLoginSession();
        } else {
            // [신규 회원]: 회원가입 진행
            member = register(provider, userInfo);
        }

        // 5. Security 에서 사용할 CustomerOAuth2User 생성하여 반환
        return CustomOAuth2User.from(member, oAuth2User.getAttributes());
    }

    // 신규 사용자 등록 (최초 로그인 시)
    private MemberEntity register(String provider, OAuth2UserInfo info) {

        MemberEntity user = MemberEntity.builder()
                .provider(provider)
                .providerId(info.getId())
                .email(info.getEmail())
                .role(UserRole.USER)
                .status(UserStatus.PENDING_PROFILE)
                .lastLoginAt(LocalDateTime.now())
                .build();

        return memberRepository.save(user);
    }
}
