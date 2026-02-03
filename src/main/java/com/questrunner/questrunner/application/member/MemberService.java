package com.questrunner.questrunner.application.member;

import com.questrunner.questrunner.api.member.dto.req.OnboardingReqDTO;
import com.questrunner.questrunner.api.member.dto.res.MemberProfileResDTO;

public interface MemberService {

    /**
     * 현재 로그인한 회원의 프로필 정보를 조회한다.
     *
     * @param memberId 조회할 회원의 고유 ID (PK)
     * @return 회원의 상세 프로필 정보 (기술 스택 포함)
     * @throws com.questrunner.questrunner.global.exception.BusinessException
     * - 해당 ID 의 회원이 존재하지 않을 경우 (MEMBER_NOT_FOUND)
     */
    MemberProfileResDTO getMyProfile(Long memberId);

    /**
     * 회원의 온보딩(프로필 완성) 프로세스를 수행한다.
     * 기본 정보(닉네임, 포지션 등)를 업데이트하고, 기술 스택을 재설정 한다.
     *
     * @param memberId 업데이트할 회원의 고유 ID (PK)
     * @param request 온보딩 폼에서 입력받은 프로필 및 기술 스택 정보
     * @throws com.questrunner.questrunner.global.exception.BusinessException
     * - 해당 ID 의 회원이 존재하지 않을 경우 (MEMBER_NOT_FOUND)
     */
    void onboard(Long memberId, OnboardingReqDTO request);
}
