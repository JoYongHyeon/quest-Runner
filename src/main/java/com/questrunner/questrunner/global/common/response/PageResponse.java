package com.questrunner.questrunner.global.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * [공통 페이지 응답 DTO]
 *
 * - Spring Data JPA 의 Page<T> 객체를 그대로 반환할 . 발생하는 직렬화 이슈를 방지
 * - 클라이언트에게 필요한 핵심 페이징 정보만 표준화된 포맷으로 제공하기 위한 래퍼 클래스.
 *
 * @param <T> 실제 데이터 리스트의 타입 (예: PartyListResDTO)
 */
public record PageResponse<T>(
        // 조회 된 데이터 리스트
        List<T> content,
        // 현재 페이지 번호 (0부터 시작)
        int pageNumber,
        // 페이지당 데이터 수
        int pageSize,
        // 전체 데이터 수
        long totalElements,
        // 전체 페이지 수
        int totalPages,
        // 마지막 페이지 여부
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
