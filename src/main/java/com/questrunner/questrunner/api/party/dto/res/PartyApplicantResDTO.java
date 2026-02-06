package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;

public record PartyApplicantResDTO(

        // 지원 식별자
        Long applicantId,
        // 지원자 ID
        Long memberId,
        // 지원자 닉네임
        String nickname,
        // 지원자 주 포지션
        String position,
        // 지원 메시지
        String message,
        // 현재 상태 (PENDING 등)
        ApplicantStatus status,
        // 지원 일시
        String appliedAt
) {

    public static PartyApplicantResDTO from(PartyApplicantEntity entity) {
        return new PartyApplicantResDTO(
                entity.getId(),
                entity.getMember().getId(),
                entity.getMember().getNickname(),
                entity.getMember().getPosition().getDesc(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt().toString()
        );
    }
}
