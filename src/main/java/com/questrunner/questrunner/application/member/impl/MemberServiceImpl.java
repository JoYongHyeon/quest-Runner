package com.questrunner.questrunner.application.member.impl;

import com.questrunner.questrunner.api.member.dto.req.OnboardingReqDTO;
import com.questrunner.questrunner.api.member.dto.res.MemberProfileResDTO;
import com.questrunner.questrunner.application.member.MemberService;
import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.member.repository.MemberRepository;
import com.questrunner.questrunner.global.enums.ErrorCode;
import com.questrunner.questrunner.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public MemberProfileResDTO getMyProfile(Long memberId) {

        // 회원 존재 여부 검증 후 조회
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResDTO.from(member);
    }

    @Override
    @Transactional
    public void onboard(Long memberId, OnboardingReqDTO request) {

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        /**
         * - 프로필 정보 업데이트
         * - 온보딩이 완료되면 내부적으로 상태가 PENDING -> ACTIVE 로 변경
         */
        member.updateOnboardingProfile(
                request.nickname(),
                request.position(),
                request.region(),
                request.intro(),
                request.gitUrl(),
                request.blogUrl(),
                request.resumeLink()
        );

        // 기술스택 ~10개 미만으로 판단하여 선택
        member.clearTechStacks();
        if (request.techStacks() != null) {
            for (String teckName : request.techStacks()) {
                member.addTechStack(teckName);
            }
        }

    }
}
