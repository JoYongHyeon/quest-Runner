package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;

import java.util.List;

public record PartyDetailResDTO(

        Long partyId,
        String title,
        String content,
        String leaderNickname,
        PartyStatus status,
        String createdAt,
        List<SlotResDTO> slots,
        List<LinkResDTO> linkList
) {

    public record LinkResDTO(String label, String url) {}

    public static PartyDetailResDTO of(PartyEntity party, List<LinkResDTO> links) {
        return new PartyDetailResDTO(
                party.getId(),
                party.getTitle(),
                party.getContent(),
                party.getLeader().getNickname(),
                party.getStatus(),
                party.getCreatedAt().toString(),
                party.getSlots().stream().map(SlotResDTO::from).toList(),
                links
        );
    }
}
