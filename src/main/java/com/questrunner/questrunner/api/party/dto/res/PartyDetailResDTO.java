package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;

import java.util.List;

public record PartyDetailResDTO(

        Long partyId,
        String title,
        String content,
        Long leaderId,
        String leaderNickname,
        PartyStatus status,
        String createdAt,
        List<SlotResDTO> slots,
        List<LinkResDTO> linkList,
        // 나의 지원 상태
        ApplicantStatus myApplicantStatus
) {

    public record LinkResDTO(String label, String url) {}


    public static PartyDetailResDTO of(PartyEntity party,
                                       List<LinkResDTO> links,
                                       ApplicantStatus myApplicantStatus,
                                       List<PartyApplicantEntity> applicants) {
        return new PartyDetailResDTO(
                party.getId(),
                party.getTitle(),
                party.getContent(),
                party.getLeader().getId(),
                party.getLeader().getNickname(),
                party.getStatus(),
                party.getCreatedAt().toString(),
                // 슬롯 변환 시 applicants 정보 전달
                party.getSlots().stream()
                        .map(slot -> SlotResDTO.of(slot, applicants))
                        .toList(),
                links,
                myApplicantStatus
        );
    }
}
