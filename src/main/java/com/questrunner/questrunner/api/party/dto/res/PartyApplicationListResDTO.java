package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;

/**
 * 내가 지원한 파티 목록 조회 응답 DTO
 */
public record PartyApplicationListResDTO(

        Long applicantId,
        Long partyId,
        String title,
        String leaderNickname,
        // 내가 지원한 포지션
        Position position,
        // 나의 지원 상태 (PENDING, ACCEPTED, REJECTED)
        ApplicantStatus status,
        // 파티의 현재 상태
        PartyStatus partyStatus,
        String appliedAt
) {

    /**
     * Entity -> DTO 변환 팩토리 메서드
     * - PartyApplicantEntity . 기반으로 필요한 정보만 추출하여 반환
     */
    public static PartyApplicationListResDTO from(PartyApplicantEntity entity) {
        return new PartyApplicationListResDTO(
                entity.getId(),
                entity.getSlot().getParty().getId(),
                entity.getSlot().getParty().getTitle(),
                entity.getSlot().getParty().getLeader().getNickname(),
                entity.getSlot().getPosition(),
                entity.getStatus(),
                entity.getSlot().getParty().getStatus(),
                entity.getCreatedAt().toString()
        );
    }
}
