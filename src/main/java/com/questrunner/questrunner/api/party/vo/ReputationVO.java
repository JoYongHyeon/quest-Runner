package com.questrunner.questrunner.api.party.vo;

/**
 * 회원 평판 통계 VO
 */
public record ReputationVO(
        // 성공 종료한 퀘스트 수
        int completedCount,
        // 강제 추방당한 횟수
        int kickedCount,
        // 자진 탈퇴한 횟수
        int quitCount,
        // 현재 진행 중인 (IN_PROGRESS) 퀘스트 수
        int activeQuestCount,
        // 가장 최근에 추방당한 사유
        String lastKickedReason
) {

    /**
     * 기본값 (0)을 가진 빈 통계 객체 생성
     */
    public static ReputationVO empty() {
        return new ReputationVO(0, 0, 0, 0, null);
    }
}
