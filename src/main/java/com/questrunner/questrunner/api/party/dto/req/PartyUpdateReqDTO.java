package com.questrunner.questrunner.api.party.dto.req;

import com.questrunner.questrunner.domain.member.vo.Position;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PartyUpdateReqDTO(

        @NotBlank(message = "파티 제목은 필수입니다.")
        @Size(min = 5, max = 100, message = "제목은 5.  100자 사이어야 합니다.")
        String title,

        @NotBlank(message = "파티 내용은 필수입니다.")
        String content,

        @NotEmpty(message = "최소 1개 이상의 모집 슬롯을 설정해야 합니다.")
        @Valid
        List<SlotUpdateReq> slots,

        @Valid
        List<LinkUpdateReq> linkList
) {

    public record SlotUpdateReq(
            Long slotId,
            @NotNull(message = "포지션은 필수입니다.")
            Position position,

            List<String> techStacks
    ) {}

    public record LinkUpdateReq(
            @NotBlank String label,
            @NotBlank String url
    ) {}
}
