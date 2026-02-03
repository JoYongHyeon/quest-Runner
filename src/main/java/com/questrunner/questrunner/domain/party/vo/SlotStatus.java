package com.questrunner.questrunner.domain.party.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 슬롯(자리) 상태
 */
@Getter
@RequiredArgsConstructor
public enum SlotStatus {

    OPEN("비어 있음 (지원 가능)"),
    LOCKED("확정됨 (사람 채워짐)"),
    CLOSED("닫힘 (파티장 권한으로 닫음)");

    private final String description;
}
