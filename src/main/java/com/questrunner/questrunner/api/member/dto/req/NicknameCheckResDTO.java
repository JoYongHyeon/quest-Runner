package com.questrunner.questrunner.api.member.dto.req;

public record NicknameCheckResDTO(
        // 사용 가능 여부 (true: 사용 가능 / false: 중복)
        boolean isAvailable
) {
}
