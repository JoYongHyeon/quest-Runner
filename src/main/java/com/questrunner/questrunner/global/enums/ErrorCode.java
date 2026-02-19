package com.questrunner.questrunner.global.enums;

import com.questrunner.questrunner.global.common.response.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ResponseCode {

    // Common (C)
    INVALID_INPUT_VALUE(400, "C-001", "올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(500, "C-002", "서버 내부 오류입니다."),

    // Auth (A)
    INVALID_TOKEN(401, "A-001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A-002", "만료된 토큰입니다."),
    NOT_EXISTS_REFRESH_TOKEN(400, "A-003", "리프레시 토큰이 존재하지 않습니다."),
    NOT_MATCH_REFRESH_TOKEN(401, "A-004", "리프레시 토큰이 일치하지 않습니다."),

    // Member (M)
    MEMBER_NOT_FOUND(404, "M-001", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(409, "M-002", "이미 존재하는 이메일입니다."),

    // Party/Slot (P)
    PARTY_NOT_FOUND(404, "P-001", "존재하지 않는 파티입니다."),
    SLOT_ALREADY_FILLED(409, "P-002", "이미 채워진 슬롯입니다."),
    PARTY_CREATION_LIMIT_EXCEEDED(409, "P-003", "이미 모집 중인 파티가 있습니다."),
    SLOT_NOT_FOUND(404, "P-004", "존재하지 않는 슬롯입니다."),
    ALREADY_APPLIED(409, "P-005", "이미 지원한 파티입니다."),
    CANNOT_APPLY_TO_OWN_PARTY(400, "P-006", "자신의 파티에는 지원할 수 없습니다."),
    NOT_PARTY_LEADER(403, "P-007", "파티장만 접근할 수 있습니다."),
    APPLICANT_NOT_FOUND(404, "P-008", "존재하지 않는 지원자입니다."),
    CANNOT_DELETE_LOCKED_SLOT(409, "P-009", "이미 승인이 확정된 슬롯은 삭제하거나 변경할 수 없습니다."),
    ALREADY_REJECTED(409, "P-010", "이미 거절된 파티입니다. 재신청할 수 없습니다."),
    NOT_MY_APPLICATION(403, "P-012", "본인의 지원 내역만 취소/탈퇴할 수 있습니다."),
    CANNOT_CANCEL_ACCEPTED(400, "P-013", "이미 승인된 지원은 취소할 수 없습니다. 탈퇴 기능을 이용해주세요."),
    CANNOT_QUIT_NOT_ACCEPTED(400, "P-014", "승인된 상태가 아니므로 탈퇴할 수 없습니다."),
    CANNOT_KICK_NOT_MEMBER(400, "P-015", "파티 멤버가 아니므로 추방할 수 없습니다.")
    ;


    private final int httpStatus;
    private final String code;
    private final String message;
}
