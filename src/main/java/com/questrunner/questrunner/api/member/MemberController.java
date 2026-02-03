package com.questrunner.questrunner.api.member;

import com.questrunner.questrunner.api.member.dto.req.OnboardingReqDTO;
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

    @GetMapping("/me")
    public ApiResponse<MemberProfileResDTO> getMyProfile(@AuthenticationPrincipal CustomOAuth2User user) {
        MemberProfileResDTO response = memberService.getMyProfile(user.memberId());

        return ApiResponse.success(SuccessCode.MY_PROFILE_READ_SUCCESS, response);
    }

    @PatchMapping("/onboarding")
    public ApiResponse<Void> completeOnboarding(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody @Valid OnboardingReqDTO request
    ) {
        memberService.onboard(user.memberId(), request);
        return ApiResponse.success(SuccessCode.ONBOARDING_UPDATE_SUCCESS, null);
    }
}
