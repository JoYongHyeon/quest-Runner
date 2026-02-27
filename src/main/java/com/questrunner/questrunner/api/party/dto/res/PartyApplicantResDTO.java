package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.api.party.vo.ReputationVO;
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
        String appliedAt,

        // 평가용  링크 정보
        String gitUrl,
        String blogUrl,
        String resumeLink,

        // 지원자 평판 데이터
        int completedCount,   // 성공 종료 횟수
        int kickedCount,      // 강제 추방 횟수
        int quitCount,        // 중도 탈퇴 횟수
        int activeQuestCount, // 현재 진행 중(IN_PROGRESS)인 퀘스트
        String lastKickedReason // 최근 추방 사유

) {

    public static PartyApplicantResDTO of(PartyApplicantEntity entity, ReputationVO reputationVO) {
        return new PartyApplicantResDTO(
                entity.getId(),
                entity.getMember().getId(),
                entity.getMember().getNickname(),
                entity.getMember().getPosition().getDesc(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt().toString(),
                entity.getMember().getGitUrl(),
                entity.getMember().getBlogUrl(),
                entity.getMember().getResumeLink(),
                reputationVO.completedCount(),
                reputationVO.kickedCount(),
                reputationVO.quitCount(),
                reputationVO.activeQuestCount(),
                reputationVO.lastKickedReason()

        );
    }
}
