package com.questrunner.questrunner.domain.member.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Position {

    BACKEND("BACKEND", "백엔드"),
    FRONTEND("FRONTEND", "프론트엔드"),
    DEVOPS("DEVOPS", "DevOps"),
    DBA("DBA", "DBA"),
    PM("PM", "기획자 (PM)"),
    DESIGN("DESIGN", "디자이너");

    private final String code;
    private final String desc;
}
