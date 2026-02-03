package com.questrunner.questrunner.domain.party.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파티 모집 상태
 */
@Getter
@RequiredArgsConstructor
public enum PartyStatus {

    RECRUITING("모징 중"),
    COMPLETED("모집 완료"),
    // 리더가 파티를 삭제/취소한 경우
    CANCELED("취소 됨");

    private final String description;
}
