package com.questrunner.questrunner.api.member.dto.res;

import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.member.entity.MemberTechStack;
import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.member.vo.Region;
import com.questrunner.questrunner.domain.member.vo.UserStatus;

import java.util.List;

public record MemberProfileResDTO(

        Long id,
        String  email,
        String nickname,
        // 계정 상태 (PENDING_PROFILE: 온보딩 전, ACTIVE: 활동 가능)
        UserStatus status,
        // 주 포지션 (BACKEND, FRONTEND ...)
        Position position,
        // 활동 지역
        Region region,
        // 보유 기술 스택 목록
        List<String> techStacks
) {

    /**
     * Entity 를 Response DTO 로 변환하는 팩토리 메서드
     * Lazy Loading 된 TechStack 을 문자열 리스트로 매핑하여 반환
     */
    public static MemberProfileResDTO from(MemberEntity member) {
        return new MemberProfileResDTO(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getStatus(),
                member.getPosition(),
                member.getRegion(),
                member.getTechStacks().stream()
                        .map(MemberTechStack::getTechName)
                        .toList()
        );
    }
}
