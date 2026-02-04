package com.questrunner.questrunner.api.party;

import com.questrunner.questrunner.api.party.dto.req.PartyApplyReqDTO;
import com.questrunner.questrunner.api.party.dto.req.PartyCreateReqDTO;
import com.questrunner.questrunner.api.party.dto.req.PartySearchCondition;
import com.questrunner.questrunner.api.party.dto.res.PartyDetailResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyListResDTO;
import com.questrunner.questrunner.application.party.PartyService;
import com.questrunner.questrunner.global.common.response.ApiResponse;
import com.questrunner.questrunner.global.enums.SuccessCode;
import com.questrunner.questrunner.global.security.oauth2.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;


    @GetMapping
    public ApiResponse<Page<PartyListResDTO>> getPartyList(
            @ModelAttribute PartySearchCondition condition,
            Pageable pageable
    ) {
        Page<PartyListResDTO> response = partyService.getPartyList(condition, pageable);

        return ApiResponse.success(SuccessCode.OK, response);
    }

    @GetMapping("/{partyId}")
    public ApiResponse<PartyDetailResDTO> getpartyDetail(@PathVariable Long partyId) {
        PartyDetailResDTO response = partyService.getPartyDetail(partyId);
        return ApiResponse.success(SuccessCode.OK, response);
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> createParty(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody @Valid PartyCreateReqDTO req
    ) {

        Long partyId = partyService.createParty(user.memberId(), req);

        // 생성된 ID 반환 (프론트에서 상세 페이지로 이동하기 위함)
        return ApiResponse.success(SuccessCode.PARTY_CREATE_SUCCESS, Map.of("partyId", partyId));
    }

    @PostMapping("/apply")
    public ApiResponse<Void> applyParty(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody @Valid PartyApplyReqDTO req
    ) {
        partyService.applyParty(user.memberId(), req);
        return ApiResponse.success(SuccessCode.PARTY_APPLY_SUCCESS, null);
    }
}
