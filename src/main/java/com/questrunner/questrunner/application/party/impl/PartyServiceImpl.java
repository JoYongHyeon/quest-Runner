package com.questrunner.questrunner.application.party.impl;

import com.questrunner.questrunner.api.party.dto.req.ApplicantDecisionReqDTO;
import com.questrunner.questrunner.api.party.dto.req.PartyApplyReqDTO;
import com.questrunner.questrunner.api.party.dto.req.PartyCreateReqDTO;
import com.questrunner.questrunner.api.party.dto.req.PartySearchCondition;
import com.questrunner.questrunner.api.party.dto.res.PartyApplicantResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyDetailResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyListResDTO;
import com.questrunner.questrunner.application.party.PartyService;
import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.member.repository.MemberRepository;
import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import com.questrunner.questrunner.domain.party.entity.PartySlotEntity;
import com.questrunner.questrunner.domain.party.repository.PartyApplicantRepository;
import com.questrunner.questrunner.domain.party.repository.PartyRepository;
import com.questrunner.questrunner.domain.party.repository.PartySlotRepository;
import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;
import com.questrunner.questrunner.domain.party.vo.SlotStatus;
import com.questrunner.questrunner.global.enums.ErrorCode;
import com.questrunner.questrunner.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;
    private final MemberRepository memberRepository;
    private final PartySlotRepository partySlotRepository;
    private final PartyApplicantRepository partyApplicantRepository;

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

    @Override
    public Page<PartyListResDTO> getPartyList(PartySearchCondition condition, Pageable pageable) {
        return partyRepository.searchParties(condition, pageable)
                .map(PartyListResDTO::from);
    }

    @Override
    public PartyDetailResDTO getPartyDetail(Long partyId) {
        PartyEntity party = partyRepository.findByIdWithAll(partyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTY_NOT_FOUND));

        return PartyDetailResDTO.from(party);
    }

    @Override
    @Transactional
    public void applyParty(Long memberId, PartyApplyReqDTO req) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        PartySlotEntity slot = partySlotRepository.findById(req.slotId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

        // 자기 파티 지원 방지 로직
        if (slot.getParty().getLeader().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.CANNOT_APPLY_TO_OWN_PARTY);
        }

        // 슬롯이 비어있는지 확인
        if (slot.getStatus() != SlotStatus.OPEN) {
            throw new BusinessException(ErrorCode.SLOT_ALREADY_FILLED);
        }

        // 중복 지원 확인 (같은 파티 내 다른 슬롯 중복 지원 불가 정책)
        if (partyApplicantRepository.existsBySlot_Party_IdAndMember_Id(slot.getParty().getId(), memberId)) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }

        PartyApplicantEntity applicant = PartyApplicantEntity.builder()
                .slot(slot)
                .member(member)
                .message(req.message())
                .build();

        partyApplicantRepository.save(applicant);
    }

    @Override
    public List<PartyApplicantResDTO> getApplicants(Long leaderId, Long partyId) {
        PartyEntity party = partyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTY_NOT_FOUND));

        // 권한 검증 : 파티장만 조회 가능
        if (!party.getLeader().getId().equals(leaderId)) {
            throw new BusinessException(ErrorCode.NOT_PARTY_LEADER);
        }

        // 해당 파티의 모든 슬롯에 대한 지원자 조회
        return partyApplicantRepository.findAllBySlot_Party_Id(partyId).stream()
                .map(PartyApplicantResDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public void decideApplicant(Long leaderId, Long applicantId, ApplicantDecisionReqDTO req) {
        PartyApplicantEntity applicant = partyApplicantRepository.findById(applicantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICANT_NOT_FOUNT));

        // 권한 검증 : 파티장만 결정 가능
        if (!applicant.getSlot().getParty().getLeader().getId().equals(leaderId)) {
            throw new BusinessException(ErrorCode.NOT_PARTY_LEADER);
        }

        // 상태 변경 분기
        if (req.status() == ApplicantStatus.ACCEPTED) {
            // 지원자 상태 -> ACCEPTED
            // 해당 슬롯 상태 -> LOCKED (마감)
            applicant.accept();
            applicant.getSlot().lock();

        } else if (req.status() == ApplicantStatus.REJECTED) {
            // 지원자 상태 -> REJECTED
            applicant.reject();
        }
    }

    @Override
    public List<PartyListResDTO> getMyParties(Long memberId) {
        return partyRepository.findAllByLeaderIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(PartyListResDTO::from)
                .toList();
    }
}
