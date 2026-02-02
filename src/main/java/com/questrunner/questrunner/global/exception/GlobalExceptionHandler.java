package com.questrunner.questrunner.global.exception;

import com.questrunner.questrunner.global.common.response.ApiResponse;
import com.questrunner.questrunner.global.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * [시스템 오류] 예상치 못한 런타임 에러
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        log.error("[Server Error] ", e);
        
        return ResponseEntity
                .status(500)
                .body(ApiResponse.failure(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * [커스텀 오류] 비즈니스 로직 상 발생하는 모든 예외
     * - BusinessException 을 상속받은 모든 예외가 이 메서드로 전달
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        // ex: ([클래스 명] 코드, 메시지)
        log.warn("[{}] code: {}, message: {}",
                e.getClass().getSimpleName(),
                e.getErrorCode().getCode(),
                e.getErrorCode().getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.failure(e.getErrorCode()));
    }
}
