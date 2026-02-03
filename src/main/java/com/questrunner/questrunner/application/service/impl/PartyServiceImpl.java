package com.questrunner.questrunner.application.service.impl;

import com.questrunner.questrunner.api.party.dto.req.PartyCreateReqDTO;
import com.questrunner.questrunner.application.service.PartyService;
import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.member.repository.MemberRepository;
import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import com.questrunner.questrunner.domain.party.entity.PartySlotEntity;
import com.questrunner.questrunner.domain.party.repository.PartyRepository;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;
import com.questrunner.questrunner.global.enums.ErrorCode;
import com.questrunner.questrunner.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Long createParty(Long leaderId, PartyCreateReqDTO req) {

        // 1. 리더(회원) 조회
        MemberEntity leader = memberRepository.findById(leaderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 파티 생성 제한 정책 확인 (현재 모집 중인 파티가 있으면 추가 생성 불가)
        // TODO : 현재는 1개이지만 추 후 결제에 따라 로직을 변경해야함 (결제 시 추가 파티 생성 가능)
        if (partyRepository.existsByLeaderIdAndStatus(leaderId, PartyStatus.RECRUITING)) {
            throw new BusinessException(ErrorCode.PARTY_CREATION_LIMIT_EXCEEDED);
        }

        // 3. 파티 엔티티 생성
        PartyEntity party = PartyEntity.builder()
                .leader(leader)
                .title(req.title())
                .content(req.content())
                .region(req.region())
                .build();

        // 4. 슬롯(모집 포지션) 추가
        for (Position pos : req.slots()) {
            party.addSlot(PartySlotEntity.builder()
                    .position(pos)
                    .build());
        }

        // 5. 저장
        partyRepository.save(party);

        return party.getId();
    }
}
