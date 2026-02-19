package com.questrunner.questrunner.application.party.impl;

import com.questrunner.questrunner.api.party.dto.req.*;
import com.questrunner.questrunner.api.party.dto.req.PartyCreateReqDTO.LinkCreateReq;
import com.questrunner.questrunner.api.party.dto.req.PartyCreateReqDTO.SlotCreateReq;
import com.questrunner.questrunner.api.party.dto.req.PartyUpdateReqDTO.LinkUpdateReq;
import com.questrunner.questrunner.api.party.dto.req.PartyUpdateReqDTO.SlotUpdateReq;
import com.questrunner.questrunner.api.party.dto.res.PartyApplicantResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyApplicationListResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyDetailResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyDetailResDTO.LinkResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyListResDTO;
import com.questrunner.questrunner.application.party.PartyService;
import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.member.repository.MemberRepository;
import com.questrunner.questrunner.domain.party.entity.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        if (partyRepository.existsByLeaderIdAndStatus(leaderId, PartyStatus.RECRUITING)) {
            throw new BusinessException(ErrorCode.PARTY_CREATION_LIMIT_EXCEEDED);
        }

        // 3. 파티 엔티티 생성
        PartyEntity party = PartyEntity.builder()
                .leader(leader)
                .title(req.title())
                .content(req.content())
                .build();

        // 4. 초대 링크
        if (req.linkList() != null) {
            for (LinkCreateReq linkReq : req.linkList()) {
                PartyInviteLinkEntity link = PartyInviteLinkEntity.builder()
                        .label(linkReq.label())
                        .url(linkReq.url())
                        .build();

                party.addLink(link);
            }
        }

        // 5. 슬롯 및 기술 스택 추가
        for (SlotCreateReq slotReq : req.slots()) {

            // 슬롯 생성
            PartySlotEntity slot = PartySlotEntity.builder()
                    .position(slotReq.position())
                    .build();

            // 슬롯 . 기술 스택 추가
            if (slotReq.techStacks() != null) {
                for (String techName : slotReq.techStacks()) {
                    PartySlotTechEntity slotTech = PartySlotTechEntity.builder()
                            .techName(techName)
                            .build();

                    slot.addTechStack(slotTech);
                }
            }
            party.addSlot(slot);
        }

        // 6. 저장
        partyRepository.save(party);

        return party.getId();
    }

    @Override
    public Page<PartyListResDTO> getPartyList(PartySearchCondition condition, Pageable pageable) {
        return partyRepository.searchParties(condition, pageable)
                .map(PartyListResDTO::from);
    }

    @Override
    public PartyDetailResDTO getPartyDetail(Long memberId, Long partyId) {
        PartyEntity party = partyRepository.findByIdWithAll(partyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTY_NOT_FOUND));

        // 1. 해당 파티의 모든 지원자 조회 (ACCEPTED 상태인 사람을 찾기 위해)
        List<PartyApplicantEntity> applicants = partyApplicantRepository.findAllBySlot_Party_Id(partyId);

        // 2. 내 지원 상태 조회 (applicants 리스트에서 찾으면 DB 조회 1회 절약 가능)
        ApplicantStatus myStatus = null;
        if (memberId != null) {
            myStatus = applicants.stream()
                    .filter(app -> app.getMember().getId().equals(memberId))
                    .findFirst()
                    .map(PartyApplicantEntity::getStatus)
                    .orElse(null);
        }

        // 3. 링크 공개 여부 확인
        // 파티장 (리더)이거나, 내 지원 상태가 '수락 (ACCEPTED)' 인 경우에만 링크를 보여줌
        boolean isLeader = party.getLeader().getId().equals(memberId);
        boolean isAcceptedMember = (myStatus == ApplicantStatus.ACCEPTED);

        List<LinkResDTO> linkDtos = new ArrayList<>();
        if (isLeader || isAcceptedMember) {
            linkDtos = party.getLinks().stream()
                    .map(link -> new LinkResDTO(link.getLabel(), link.getUrl()))
                    .toList();
        }

        // 4. DTO 반환 (나의 지원 상태 포함)
        return PartyDetailResDTO.of(party, linkDtos, myStatus, applicants);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICANT_NOT_FOUND));

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

    @Override
    @Transactional
    public void updateParty(Long leaderId, Long partyId, PartyUpdateReqDTO req) {
        PartyEntity party = partyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTY_NOT_FOUND));

        // 1. 권한 검증
        if (!party.getLeader().getId().equals(leaderId)) {
            throw new BusinessException(ErrorCode.NOT_PARTY_LEADER);
        }

        // 2. 기본 정보 업데이트
        party.updateContent(req.title(), req.content());

        // 3. 링크 교체 (전체 삭제 후 재생성)
        List<PartyInviteLinkEntity> newLinks = new ArrayList<>();
        if (req.linkList() != null) {
            for (LinkUpdateReq linkReq : req.linkList()) {
                newLinks.add(PartyInviteLinkEntity.builder()
                        .label(linkReq.label())
                        .url(linkReq.url())
                        .build());
            }
        }
        party.replaceLinks(newLinks);


        // 4. 슬롯 업데이트 로직
        List<PartySlotEntity> currentSlots = party.getSlots();

        // 4-1. 요청 데이터 분석
        // 요청에 포함된 ID 목록 추출 (유요한 기존 슬롯 ID 들)
        List<Long> reqSlotIds = req.slots().stream()
                .map(SlotUpdateReq::slotId)
                .filter(Objects::nonNull)
                .toList();

        // 4-2. 삭제할 슬롯 처리 (DB에  있는데 요청에는 없는 슬롯)
        // 단, LOCKED 상태인 슬롯이 삭제 대상에 포함되면 예외 발생
        List<PartySlotEntity> toDelete = currentSlots.stream()
                .filter(slot -> !reqSlotIds.contains(slot.getId()))
                .toList();

        for (PartySlotEntity slot : toDelete) {
            // [정책 변경] LOCKED (승인 완료) 된 슬롯도 파티장이 원하면 삭제 가능하도록 정책 완화.
            // 단 해당 슬롯에 연결된 지원자 (Applicant) 정보도 함께 삭제됨

            // 1. 지원자 데이터 삭제 (OPEN, LOCKED 상관 없이 모두 삭제)
            partyApplicantRepository.deleteAllBySlot_Id(slot.getId());

            // 2. 슬롯 리스트에서 제거
            currentSlots.remove(slot);
        }

        // 4-3. 생성 및 수정 처리
        for (SlotUpdateReq slotReq : req.slots()) {
            if (slotReq.slotId() == null) {
                // [신규 생성] ID 가 없으면 새 슬롯
                PartySlotEntity newSlot = PartySlotEntity.builder()
                        .position(slotReq.position())
                        .build();

                addTechStacks(newSlot, slotReq.techStacks());
                party.addSlot(newSlot);

            } else {
                // [기존 수정] ID 가 있으면 찾아서 업데이트
                PartySlotEntity existingSlot = currentSlots.stream()
                        .filter(s -> s.getId().equals(slotReq.slotId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

                // LOCKED 슬롯은 수정 불가 (포지션/스택 변경 금지)
                if (existingSlot.getStatus() == SlotStatus.LOCKED) {
                    // 요청 된 내용과 기존 내용이 다르면 에러
                    if (existingSlot.getPosition() != slotReq.position()) {
                        throw new BusinessException(ErrorCode.CANNOT_DELETE_LOCKED_SLOT);
                    }
                    // 변경 없이 넘어감
                    continue;
                }

                // OPEN 슬롯은 정보 업데이트 (TechStack 은 전체 교체)
                existingSlot.updatePosition(slotReq.position());

                // 기존 스택 비우기
                existingSlot.getTechStacks().clear();
                addTechStacks(existingSlot, slotReq.techStacks());
            }
        }
    }

    @Override
    public List<PartyApplicationListResDTO> getMyAppliedParties(Long memberId) {
        // 회원이 지원한 모든 내역을 조회하여 DTO 로 변환
        return partyApplicantRepository.findAllByMemberId(memberId).stream()
                .map(PartyApplicationListResDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public void cancelApplication(Long memberId, Long applicantId) {
        // 1. 조회
        PartyApplicantEntity applicant = partyApplicantRepository.findById(applicantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICANT_NOT_FOUND));

        // 2. 권한 검증 (본인 확인)
        applicant.validateOwner(memberId);

        // 3. 상태 검증 (PENDING 상태만  삭제 가능)
        if (applicant.getStatus() != ApplicantStatus.PENDING) {
            // "이미 수락되었거나 거절된 지원입니다." 에러 코드
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ACCEPTED);
        }

        // 4. 로직 수행 (삭제)
        partyApplicantRepository.delete(applicant);

    }

    @Override
    @Transactional
    public void quitParty(Long memberId, Long applicantId) {
        // 1. 조회
        PartyApplicantEntity applicant = partyApplicantRepository.findById(applicantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICANT_NOT_FOUND));

        // 2. 권한 검증
        applicant.validateOwner(memberId);

        // 3. 상태 검증
        applicant.quit();

        // 4. 로직 수행 (상태 변경 -> Entity 위임)
        applicant.quit();
    }

    @Override
    @Transactional
    public void kickApplicant(Long leaderId, Long applicantId, PartyKickReqDTO req) {
        // 1. 지원 내역 조회
        PartyApplicantEntity applicant = partyApplicantRepository.findById(applicantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICANT_NOT_FOUND));

        // 2. 권한 검증 (파티장만 가능)
        if (!applicant.getSlot().getParty().getLeader().getId().equals(leaderId)) {
            throw new BusinessException(ErrorCode.NOT_PARTY_LEADER);
        }

        // 3. 상태 검증 (이미 멤버인 사람만 추방 가능)
        if (applicant.getStatus() != ApplicantStatus.ACCEPTED) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_NOT_MEMBER);
        }

        // 4. 추방 처리 (엔티티 위임)
        applicant.kick(req.reason());

        // 5. 슬롯 다시. 열기 (새 멤버 모집을 위해)
        // ACCEPTED 상태 였다면 해당 슬롯은 LOCKED 였을 것이므로, 다시 OPEN 상태로 변경
        applicant.getSlot().open();
    }

    // 기술 스택 추가 헬퍼 메서드
    private void addTechStacks(PartySlotEntity slot, List<String> techStacks) {
        if (techStacks != null) {
            for (String techName : techStacks) {
                slot.addTechStack(PartySlotTechEntity.builder()
                        .techName(techName)
                        .build());
            }
        }
    }
}
