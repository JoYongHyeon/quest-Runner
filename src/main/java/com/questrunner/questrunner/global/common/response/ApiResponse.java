package com.questrunner.questrunner.global.common.response;

/**
 * API 공통 응답 규격을 정의
 */
public record ApiResponse<T>(
        String code,
        String message,
        T data
) {

    // 0. 가장 많이 쓰이는 성공 응답 (HTTP 200 기반)
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("200", "OK", data);
    }

    // 1. 커스텀 성공 응답 (성공 코드 정의 시 사용)
    public static <T> ApiResponse<T> success(ResponseCode code, T data) {
        return new ApiResponse<>(code.getCode(), code.getMessage(), data);
    }

    // 2. 표준 실패 응답
    public static <T> ApiResponse<T> failure(ResponseCode code) {
        return new ApiResponse<>(code.getCode(), code.getMessage(), null);
    }

    // 3. 커스텀 메시지가 필요한 실패 응답 (주로 유효성 검사 등)
    public static <T> ApiResponse<T> error(ResponseCode code, String message) {
        return new ApiResponse<>(code.getCode(), message, null);
    }
}
