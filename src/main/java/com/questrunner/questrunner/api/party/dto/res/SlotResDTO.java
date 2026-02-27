package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.api.party.vo.ReputationVO;
import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import com.questrunner.questrunner.domain.party.entity.PartySlotEntity;
import com.questrunner.questrunner.domain.party.entity.PartySlotTechEntity;
import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;
import com.questrunner.questrunner.domain.party.vo.SlotStatus;

import java.util.List;
import java.util.Map;

public record SlotResDTO(

        Long slotId,
        Position position,
        SlotStatus status,
        List<String> techStacks,
        // 매칭된 멤버 정보 (LOCKED 상태일 때만 존재)
        MatchedMemberDTO matchedMember,
        List<PartyApplicantResDTO> applicants
) {

    public record MatchedMemberDTO(
            Long memberId,
            String nickname,
            String gitUrl,
            String blogUrl,
            Long applicantId
    ) {}


    /**
     * 단순 변환용 메서드
     */
    public static SlotResDTO from(PartySlotEntity slot) {
        return of(slot, List.of(), false, Map.of());
    }

    /**
     * Entity -> DTO 변환 (리더 여부에 따른 정보 노출 제어 및 평판 정보 주입)
     */
    public static SlotResDTO of(PartySlotEntity slot,
                                List<PartyApplicantEntity> allApplicants,
                                boolean isLeader,
                                Map<Long, ReputationVO> reputations) {

        // 1. 매칭된 멤버 정보 (LOCKED 상태일 때만)
        MatchedMemberDTO matchedMember = null;

        // 이 슬롯에 대해 ACCEPTED 된 지원자가 있는지 확인
        if (slot.getStatus() == SlotStatus.LOCKED) {
            matchedMember = allApplicants.stream()
                    .filter(app -> app.getSlot().getId().equals(slot.getId()) && app.getStatus() == ApplicantStatus.ACCEPTED)
                    .findFirst()
                    .map(app -> new MatchedMemberDTO(
                            app.getMember().getId(),
                            app.getMember().getNickname(),
                            app.getMember().getGitUrl(),
                            app.getMember().getBlogUrl(),
                            app.getId()
                    ))
                    .orElse(null);
        }

        // 2. 지원자 명단 추출 (리더이고 OPEN 상태일 때만 공개)
        List<PartyApplicantResDTO> slotApplicants = null;
        if (isLeader && slot.getStatus() == SlotStatus.OPEN) {
            slotApplicants = allApplicants.stream()
                    .filter(app -> app.getSlot().getId().equals(slot.getId()))
                    // 오직 '대기 중 (PENDING)' 인 지원자만 관리 목록에 노출
                    .filter(app -> app.getStatus() == ApplicantStatus.PENDING)
                    // 계산된 평판 VO 매핑 (없으면 empty)
                    .map(app -> PartyApplicantResDTO.of(app, reputations.getOrDefault(app.getMember().getId(), ReputationVO.empty())))
                    .toList();
        }

        return new SlotResDTO(
                slot.getId(),
                slot.getPosition(),
                slot.getStatus(),
                slot.getTechStacks().stream()
                        .map(PartySlotTechEntity::getTechName)
                        .toList(),
                matchedMember,
                slotApplicants);
    }
}
