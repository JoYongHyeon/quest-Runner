package com.questrunner.questrunner.global.enums;

import com.questrunner.questrunner.global.common.response.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements ResponseCode {

    // --- 공통 ---
    OK(200, "C-S001", "요청이 성공적으로 처리되었습니다."),
    CREATED(201, "C-S002", "리소스가 성공적으로 생성되었습니다."),

    // --- 회원 ---
    MEMBER_SIGUP_SUCCESS(201, "M-S001", "회원가입이 완료되었습니다."),
    ONBOARDING_UPDATE_SUCCESS(200, "M-S002", "프로필 설정이 성공적으로 저장되었습니다."),
    MY_PROFILE_READ_SUCCESS(200, "M-S003", "회원 정보 조회에 성공했습니다."),

    // --- 파티 ---
    PARTY_CREATE_SUCCESS(201, "P-S001", "파티가 성공적으로 생성되었습니다."),
    ;

    private final int httpStatus;
    private final String code;
    private final String message;
}
