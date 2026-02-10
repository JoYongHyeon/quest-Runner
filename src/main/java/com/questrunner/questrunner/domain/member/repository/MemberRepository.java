package com.questrunner.questrunner.domain.member.repository;

import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);

    // provider + providerId 로 OAuth2 사용자 조회
    Optional<MemberEntity> findByProviderAndProviderId(String provider, String providerId);
}
