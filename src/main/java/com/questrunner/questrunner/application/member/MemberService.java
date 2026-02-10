package com.questrunner.questrunner.application.member;

import com.questrunner.questrunner.api.member.dto.req.MemberProfileReqDTO;
import com.questrunner.questrunner.api.member.dto.res.MemberProfileResDTO;

public interface MemberService {


    /**
     * 닉네임 존재 여부 확인
     *
     * @param nickname 중복 확인할 닉네임
     * @return 존재하면 true, 없으면 false
     */
    boolean checkNicknameAvailability(String nickname);

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
     * 회원의 프로필 정보를 업데이트
     *
     * @param memberId 업데이트할 회원의 고유 ID (PK)
     * @param request 프로필 수정 요청 DTO
     * @throws com.questrunner.questrunner.global.exception.BusinessException
     * - 해당 ID 의 회원이 존재하지 않을 경우 (MEMBER_NOT_FOUND)
     */
    void updateProfile(Long memberId, MemberProfileReqDTO request);
}
