package com.questrunner.questrunner.global.common.response;

/**
 * 모든 비즈니스 응답 코드(성공/실패)가 공통적으로 가져야 할 규격을 정의
 */
public interface ResponseCode {
    int getHttpStatus();
    String getCode();
    String getMessage();
}
