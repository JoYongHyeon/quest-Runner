package com.questrunner.questrunner.api.party.dto.req;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PartyApplyReqDTO(
        @NotNull(message = "지원할 슬롯 ID는 필수입니다.")
        Long slotId,

        @Size(max = 100, message = "지원 메시지는 100자 이내로 작성해주세요.")
        String message
) {}
