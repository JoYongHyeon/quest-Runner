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

    // 파티 기본 정보 및 검색
    private final PartyRepository partyRepository;
    // 회원 정보 및 권한 확인
    private final MemberRepository memberRepository;
    // 파티 내 모집 슬롯 관리
    private final PartySlotRepository partySlotRepository;
    // 지원 내역 및 상태 관리
    private final PartyApplicantRepository partyApplicantRepository;

    @Override
    @Transactional
    public Long createParty(Long leaderId, PartyCreateReqDTO req) {
        // 리더 존재 확인
        MemberEntity leader = getMember(leaderId);

        // [Policy] 무료 유저는 동시에 1개의 '모집 중' 파티만 소유 가능
        if (partyRepository.existsByLeaderIdAndStatus(leaderId, PartyStatus.RECRUITING)) {
            throw new BusinessException(ErrorCode.PARTY_CREATION_LIMIT_EXCEEDED);
        }

        PartyEntity party = PartyEntity.builder()
                .leader(leader).title(req.title()).content(req.content()).build();

        // 초대 링크 및 모집 슬롯 일괄 생성 (헬퍼 메서드 활용)
        addLinksToParty(party, req.linkList());
        addSlotsToParty(party, req.slots());

        return partyRepository.save(party).getId();
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

        List<PartyApplicantEntity> applicants = partyApplicantRepository.findAllBySlot_Party_Id(partyId);

        // 내 지원 상태 조회 (비로그인 시 null)
        ApplicantStatus myStatus = (memberId == null) ? null : applicants.stream()
                .filter(app -> app.getMember().getId().equals(memberId))
                .findFirst()
                .map(PartyApplicantEntity::getStatus)
                .orElse(null);

        // 권한 확인: 리더이거나 승인된 팀원인 경우에만 초대 링크 공개
        boolean isAuthorized = isAuthorizedToSeeLinks(party, memberId, myStatus);
        List<PartyDetailResDTO.LinkResDTO> linkDtos = isAuthorized
                ? party.getLinks().stream().map(l -> new PartyDetailResDTO.LinkResDTO(l.getLabel(), l.getUrl())).toList()
                : new ArrayList<>();

        return PartyDetailResDTO.of(party, linkDtos, myStatus, applicants);
    }

    @Override
    @Transactional
    public void applyParty(Long memberId, PartyApplyReqDTO req) {
        MemberEntity member = getMember(memberId);
        PartySlotEntity slot = partySlotRepository.findById(req.slotId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

        PartyEntity party = slot.getParty();

        // [Guard] 취소/종료된 파티는 신규 지원 불가
        validatePartyIsActive(party);

        if (party.getLeader().getId().equals(memberId))
            throw new BusinessException(ErrorCode.CANNOT_APPLY_TO_OWN_PARTY);

        if (slot.getStatus() != SlotStatus.OPEN)
            throw new BusinessException(ErrorCode.SLOT_ALREADY_FILLED);

        if (partyApplicantRepository.existsBySlot_Party_IdAndMember_Id(party.getId(), memberId)) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }

        partyApplicantRepository.save(PartyApplicantEntity.builder()
                .slot(slot).member(member).message(req.message()).build());
    }

    @Override
    @Transactional
    public void decideApplicant(Long leaderId, Long applicantId, ApplicantDecisionReqDTO req) {
        PartyApplicantEntity applicant = getApplicant(applicantId);
        PartyEntity party = applicant.getSlot().getParty();

        validateOwner(party, leaderId);
        validatePartyIsActive(party); // 취소된 파티는 결정 불가

        if (req.status() == ApplicantStatus.ACCEPTED) {
            applicant.accept();
            applicant.getSlot().lock(); // 멤버 확정 시 슬롯 잠금
        } else {
            applicant.reject();
        }
    }

    @Override
    @Transactional
    public void updateParty(Long leaderId, Long partyId, PartyUpdateReqDTO req) {
        PartyEntity party = getParty(partyId);
        validateOwner(party, leaderId);
        // 취소되거나 완료된 파티는 수정 불가
        validatePartyIsActive(party);

        party.updateContent(req.title(), req.content());
        updatePartyLinks(party, req.linkList());
        updatePartySlots(party, req.slots()); // 지능형 슬롯 업데이트 호출
    }

    @Override
    @Transactional
    public void kickApplicant(Long leaderId, Long applicantId, PartyKickReqDTO req) {
        PartyApplicantEntity applicant = getApplicant(applicantId);
        validateOwner(applicant.getSlot().getParty(), leaderId);
        validatePartyIsActive(applicant.getSlot().getParty()); // 종료된 파티 인터랙션 차단

        if (applicant.getStatus() != ApplicantStatus.ACCEPTED)
            throw new BusinessException(ErrorCode.CANNOT_KICK_NOT_MEMBER);

        applicant.kick(req.reason());
        applicant.getSlot().open(); // 자리 다시 열기
    }

    @Override
    @Transactional
    public void quitParty(Long memberId, Long applicantId) {
        PartyApplicantEntity applicant = getApplicant(applicantId);
        validatePartyIsActive(applicant.getSlot().getParty()); // 이미 종료된 여정은 탈퇴 행위 차단

        if (!applicant.getMember().getId().equals(memberId)) throw new BusinessException(ErrorCode.NOT_MY_APPLICATION);

        applicant.quit();
    }

    @Override
    @Transactional
    public void startQuest(Long leaderId, Long partyId) {
        PartyEntity party = getParty(partyId);
        validateOwner(party, leaderId);
        validatePartyIsActive(party);

        // 최소 1명 이상의 확정된 팀원이 있어야 퀘스트 시작 가능 (평판 조작 방지)
        boolean hasMembers = party.getSlots().stream()
                .anyMatch(s -> s.getStatus() == SlotStatus.LOCKED);
        if (!hasMembers) throw new BusinessException(ErrorCode.NOT_ENOUGH_MEMBERS);

        party.start();
    }

    @Override
    @Transactional
    public void completeQuest(Long leaderId, Long partyId) {
        PartyEntity party = getParty(partyId);
        validateOwner(party, leaderId);
        // 퀘스트 성공 종료
        party.complete();
    }

    @Override
    @Transactional
    public void cancelParty(Long leaderId, Long partyId) {
        PartyEntity party = getParty(partyId);
        validateOwner(party, leaderId);
        validatePartyIsActive(party); // 이미 종료된 퀘스트는 다시 취소 불가

        party.cancel();
    }

    @Override
    public List<PartyListResDTO> getMyParties(Long memberId) {
        return partyRepository.findAllByLeaderIdOrderByCreatedAtDesc(memberId)
                .stream().map(PartyListResDTO::from).toList();
    }

    @Override
    public List<PartyApplicationListResDTO> getMyAppliedParties(Long memberId) {
        return partyApplicantRepository.findAllByMemberId(memberId).stream()
                .map(PartyApplicationListResDTO::from).toList();
    }

    @Override
    @Transactional
    public void cancelApplication(Long memberId, Long applicantId) {
        PartyApplicantEntity applicant = getApplicant(applicantId);
        if (!applicant.getMember().getId().equals(memberId)) throw new BusinessException(ErrorCode.NOT_MY_APPLICATION);
        if (applicant.getStatus() != ApplicantStatus.PENDING)
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ACCEPTED);

        partyApplicantRepository.delete(applicant);
    }

    @Override
    public List<PartyApplicantResDTO> getApplicants(Long leaderId, Long partyId) {
        PartyEntity party = getParty(partyId);
        validateOwner(party, leaderId);

        return partyApplicantRepository.findAllBySlot_Party_Id(partyId).stream()
                .map(PartyApplicantResDTO::from).toList();
    }

    // --- [Helper Methods] ---

    /**
     * [Helper] 파티 엔티티 단건 조회
     */
    private PartyEntity getParty(Long partyId) {
        return partyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTY_NOT_FOUND));
    }

    /**
     * [Helper] 지원 내역 엔티티 단건 조회
     */
    private PartyApplicantEntity getApplicant(Long applicantId) {
        return partyApplicantRepository.findById(applicantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICANT_NOT_FOUND));
    }

    /**
     * [Helper] 회원 엔티티 단건 조회
     */
    private MemberEntity getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * [Helper] 파티장 소유권 검증
     */
    private void validateOwner(PartyEntity party, Long memberId) {
        if (!party.getLeader().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.NOT_PARTY_LEADER);
        }
    }

    /**
     * [Active Guard] 파티가 수정 가능한 '활성' 상태인지 검증 (CANCELED/COMPLETED 차단)
     */
    private void validatePartyIsActive(PartyEntity party) {
        if (party.getStatus() == PartyStatus.CANCELED || party.getStatus() == PartyStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_CLOSED_PARTY);
        }
    }

    /**
     * 링크 열람 권한 확인 (리더 또는 승인된 팀원 전용)
     */
    private boolean isAuthorizedToSeeLinks(PartyEntity party, Long memberId, ApplicantStatus status) {
        if (memberId == null) return false;
        return party.getLeader().getId().equals(memberId) || status == ApplicantStatus.ACCEPTED;
    }

    /**
     * [Helper] 파티 생성 시 링크 일괄 추가
     */
    private void addLinksToParty(PartyEntity party, List<LinkCreateReq> linkList) {
        if (linkList == null) return;
        linkList.forEach(l -> party.addLink(PartyInviteLinkEntity.builder().label(l.label()).url(l.url()).build()));
    }

    /**
     * [Helper] 파티 생성 시 슬롯 일괄 추가
     */
    private void addSlotsToParty(PartyEntity party, List<SlotCreateReq> slots) {
        slots.forEach(s -> {
            PartySlotEntity slot = PartySlotEntity.builder().position(s.position()).build();
            if (s.techStacks() != null) {
                s.techStacks().forEach(ts -> slot.addTechStack(PartySlotTechEntity.builder().techName(ts).build()));
            }
            party.addSlot(slot);
        });
    }

    /**
     * [Helper] 파티 수정 시 초대 링크 전면 교체
     */
    private void updatePartyLinks(PartyEntity party, List<LinkUpdateReq> linkList) {
        List<PartyInviteLinkEntity> newLinks = new ArrayList<>();
        if (linkList != null) {
            linkList.forEach(l -> newLinks.add(PartyInviteLinkEntity.builder().label(l.label()).url(l.url()).build()));
        }
        party.replaceLinks(newLinks);
    }

    /**
     * [Helper] 파티 수정 시 슬롯 지능형 업데이트 (LOCKED 슬롯 무결성 보호)
     */
    private void updatePartySlots(PartyEntity party, List<SlotUpdateReq> reqSlots) {
        List<PartySlotEntity> currentSlots = party.getSlots();
        List<Long> reqSlotIds = reqSlots.stream().map(SlotUpdateReq::slotId).filter(Objects::nonNull).toList();

        // 1. 삭제: 요청 DTO 에 없는 기존 슬롯 제거 (연결된 지원자 데이터 동시 삭제)
        currentSlots.removeIf(slot -> {
            if (!reqSlotIds.contains(slot.getId())) {
                partyApplicantRepository.deleteAllBySlot_Id(slot.getId());
                return true;
            }
            return false;
        });

        // 2. 갱신: 신규 슬롯 생성 또는 기존 슬롯 수정
        reqSlots.forEach(slotReq -> {
            if (slotReq.slotId() == null) {
                PartySlotEntity newSlot = PartySlotEntity.builder().position(slotReq.position()).build();
                addTechStacksToSlot(newSlot, slotReq.techStacks());
                party.addSlot(newSlot);
            } else {
                PartySlotEntity existingSlot = currentSlots.stream()
                        .filter(s -> s.getId().equals(slotReq.slotId())).findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

                // [보호] 승인 확정된 슬롯은 포지션 변경 시 에러 발생
                if (existingSlot.getStatus() == SlotStatus.LOCKED) {
                    if (existingSlot.getPosition() != slotReq.position())
                        throw new BusinessException(ErrorCode.CANNOT_DELETE_LOCKED_SLOT);
                    return;
                }
                existingSlot.updatePosition(slotReq.position());
                existingSlot.getTechStacks().clear(); // 기술 스택 전면 재구성
                addTechStacksToSlot(existingSlot, slotReq.techStacks());
            }
        });
    }

    /**
     * [Helper] 개별 슬롯에 기술 스택 추가
     */
    private void addTechStacksToSlot(PartySlotEntity slot, List<String> techStacks) {
        if (techStacks != null) {
            techStacks.forEach(ts -> slot.addTechStack(PartySlotTechEntity.builder().techName(ts).build()));
        }
    }
}
