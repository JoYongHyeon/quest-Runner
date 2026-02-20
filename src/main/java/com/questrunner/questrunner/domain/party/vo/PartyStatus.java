package com.questrunner.questrunner.domain.party.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파티 모집 상태
 */
@Getter
@RequiredArgsConstructor
public enum PartyStatus {

    RECRUITING("모집 중"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료(성공)"),
    CANCELED("취소(실패)");

    private final String description;
}
