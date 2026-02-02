package com.questrunner.questrunner.domain.member.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Position {

    BACKEND("BACKEND", "백엔드"),
    FRONTEND("FRONTEND", "프론트엔드"),
    DESIGN("DESIGN", "디자인"),
    PM("PM", "기획/PM"),
    DEVOPS("DEVOPS", "데브옵스"),
    DATA("DATA", "데이터"),
    ETC("ETC", "기타");

    private final String code;
    private final String desc;
}
