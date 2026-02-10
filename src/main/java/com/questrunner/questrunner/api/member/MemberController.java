package com.questrunner.questrunner.api.member;

import com.questrunner.questrunner.api.member.dto.req.MemberProfileReqDTO;
import com.questrunner.questrunner.api.member.dto.req.NicknameCheckResDTO;
import com.questrunner.questrunner.api.member.dto.res.MemberProfileResDTO;
import com.questrunner.questrunner.application.member.MemberService;
import com.questrunner.questrunner.global.common.response.ApiResponse;
import com.questrunner.questrunner.global.enums.SuccessCode;
import com.questrunner.questrunner.global.security.oauth2.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/check-nickname")
    public ApiResponse<NicknameCheckResDTO> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = memberService.checkNicknameAvailability(nickname);

        return ApiResponse.success(SuccessCode.OK, new NicknameCheckResDTO(isAvailable));
    }

    @GetMapping("/me")
    public ApiResponse<MemberProfileResDTO> getMyProfile(@AuthenticationPrincipal CustomOAuth2User user) {
        MemberProfileResDTO response = memberService.getMyProfile(user.memberId());

        return ApiResponse.success(SuccessCode.MY_PROFILE_READ_SUCCESS, response);
    }

    @PatchMapping("/profile")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody @Valid MemberProfileReqDTO request
    ) {
        memberService.updateProfile(user.memberId(), request);
        return ApiResponse.success(SuccessCode.PROFILE_UPDATE_SUCCESS, null);
    }
}
