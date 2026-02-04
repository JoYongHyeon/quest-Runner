package com.questrunner.questrunner.domain.party.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicantStatus {

    PENDING("대기 중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨");

    private final String description;
}
