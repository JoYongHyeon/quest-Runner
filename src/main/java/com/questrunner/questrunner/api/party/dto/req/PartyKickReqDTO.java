package com.questrunner.questrunner.api.party.dto.req;

import jakarta.validation.constraints.NotBlank;

public record PartyKickReqDTO(
        @NotBlank(message = "추방 사유는 필수입니다.")
        String reason
) {}
