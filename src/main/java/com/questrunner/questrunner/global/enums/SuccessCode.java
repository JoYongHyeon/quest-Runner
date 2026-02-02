package com.questrunner.questrunner.global.enums;

import com.questrunner.questrunner.global.common.response.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements ResponseCode {

    ;

    private final int httpStatus;
    private final String code;
    private final String message;
}
