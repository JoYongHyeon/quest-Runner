package com.questrunner.questrunner.api.party.dto.req;

import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicantDecisionReqDTO(

        // ACCEPTED or REJECTED
        @NotNull(message = "결정 상태는 필수입니다.")
        ApplicantStatus status
) {}
