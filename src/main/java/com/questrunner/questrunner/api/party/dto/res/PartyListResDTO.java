package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.domain.party.entity.PartyEntity;

import java.util.List;

public record PartyListResDTO(

        Long partyId,
        String title,
        String leaderNickname,
        String region,
        String status,
        List<SlotResDTO> slots,
        String createdAt
) {

    public static PartyListResDTO from(PartyEntity party) {
        return new PartyListResDTO(
                party.getId(),
                party.getTitle(),
                party.getLeader().getNickname(),
                party.getRegion().getDesc(),
                party.getStatus().getDescription(),
                party.getSlots().stream().map(SlotResDTO::from).toList(),
                party.getCreatedAt().toString()
        );
    }
}
