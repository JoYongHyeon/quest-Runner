package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import com.questrunner.questrunner.domain.party.entity.PartySlotEntity;
import com.questrunner.questrunner.domain.party.entity.PartySlotTechEntity;
import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;
import com.questrunner.questrunner.domain.party.vo.SlotStatus;

import java.util.List;

public record SlotResDTO(

        Long slotId,
        Position position,
        SlotStatus status,
        List<String> techStacks,
        // 매칭된 멤버 정보 (LOCKED 상태일 때만 존재)
        MatchedMemberDTO matchedMember
) {

    public record MatchedMemberDTO(
            Long memberId,
            String nickname,
            String gitUrl,
            String blogUrl
    ) {}


    public static SlotResDTO from(PartySlotEntity slot) {
        return of(slot, List.of());
    }
    /**
     * Entity -> DTO 변환 (applicants 리스트를 받아서 매칭된 사람 찾기)
     */
    public static SlotResDTO of(PartySlotEntity slot, List<PartyApplicantEntity> applicants) {

        MatchedMemberDTO matchedMember = null;

        // 이 슬롯에 대해 ACCEPTED 된 지원자가 있는지 확인
        if (slot.getStatus() == SlotStatus.LOCKED) {
            matchedMember = applicants.stream()
                    .filter(app -> app.getSlot().getId().equals(slot.getId())
                                && app.getStatus() == ApplicantStatus.ACCEPTED)
                    .findFirst()
                    .map(app -> new MatchedMemberDTO(
                            app.getMember().getId(),
                            app.getMember().getNickname(),
                            app.getMember().getGitUrl(),
                            app.getMember().getBlogUrl()
                    ))
                    .orElse(null);
        }
        return new SlotResDTO(
                slot.getId(),
                slot.getPosition(),
                slot.getStatus(),
                slot.getTechStacks().stream()
                        .map(PartySlotTechEntity::getTechName)
                        .toList(),
                matchedMember);
    }
}
