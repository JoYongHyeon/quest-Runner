package com.questrunner.questrunner.application.member.impl;

import com.questrunner.questrunner.api.member.dto.req.MemberProfileReqDTO;
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
    public boolean checkNicknameAvailability(String nickname) {
        // 존재하면 false(사용 불가), 없으면 true(사용 가능)
        return !memberRepository.existsByNickname(nickname);
    }

    @Override
    public MemberProfileResDTO getMyProfile(Long memberId) {

        // 회원 존재 여부 검증 후 조회
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResDTO.from(member);
    }

    @Override
    @Transactional
    public void updateProfile(Long memberId, MemberProfileReqDTO request) {

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        /**
         * - 프로필 정보 업데이트
         * - 프로필 추가 입력이 완료되면 내부적으로 상태가 PENDING -> ACTIVE 로 변경
         */
        member.updateProfile(
                request.nickname(),
                request.position(),
                request.intro(),
                request.gitUrl(),
                request.blogUrl(),
                request.resumeLink()
        );

        // 기술 스택 재설정 (DELETE -> INSERT 순서 보장 로직)
        member.clearTechStacks();

        // 강제 Flush: DELETE 쿼리를 DB에 먼저 전송하여 Unique 충돌 방지
        memberRepository.flush();

        if (request.techStacks() != null) {
            for (String techName : request.techStacks()) {
                member.addTechStack(techName);
            }
        }

    }
}
