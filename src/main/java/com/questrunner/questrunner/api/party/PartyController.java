package com.questrunner.questrunner.api.party;

import com.questrunner.questrunner.api.party.dto.req.PartyCreateReqDTO;
import com.questrunner.questrunner.application.service.PartyService;
import com.questrunner.questrunner.global.common.response.ApiResponse;
import com.questrunner.questrunner.global.enums.SuccessCode;
import com.questrunner.questrunner.global.security.oauth2.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    @PostMapping
    public ApiResponse<Map<String, Long>> createParty(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody @Valid PartyCreateReqDTO req
            ) {

        Long partyId = partyService.createParty(user.memberId(), req);

        // 생성된 ID 반환 (프론트에서 상세 페이지로 이동하기 위함)
        return ApiResponse.success(SuccessCode.PARTY_CREATE_SUCCESS, Map.of("partyId", partyId));
    }
}
