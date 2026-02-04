package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.domain.member.vo.Region;
import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;

import java.util.List;

public record PartyDetailResDTO(

        Long partyId,
        String title,
        String content,
        String leaderNickname,
        Region region,
        PartyStatus status,
        String createdAt,
        // 슬롯 리스트 (상세 정보 포함)
        List<SlotResDTO> slots
) {
    public static PartyDetailResDTO from(PartyEntity party) {
        return new PartyDetailResDTO(
                party.getId(),
                party.getTitle(),
                party.getContent(),
                party.getLeader().getNickname(),
                party.getRegion(),
                party.getStatus(),
                party.getCreatedAt().toString(),
                party.getSlots().stream().map(SlotResDTO::from).toList()
        );
    }
}
