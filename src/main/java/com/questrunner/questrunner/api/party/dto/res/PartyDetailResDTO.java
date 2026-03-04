package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.api.party.vo.ReputationVO;
import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;

import java.util.List;
import java.util.Map;

public record PartyDetailResDTO(

        Long partyId,
        String title,
        String content,
        Long leaderId,
        String leaderNickname,
        PartyStatus status,
        String createdAt,
        String startedAt,
        String completedAt,
        List<SlotResDTO> slots,
        List<LinkResDTO> linkList,
        // 나의 지원 상태
        ApplicantStatus myApplicantStatus
) {

    public record LinkResDTO(String label, String url) {}


    /**
     * 상세 응답 DTO 생성 팩토리 메서드
     */
    public static PartyDetailResDTO of(PartyEntity party,
                                       List<LinkResDTO> links,
                                       ApplicantStatus myApplicantStatus,
                                       List<PartyApplicantEntity> applicants,
                                       boolean isLeader,
                                       Map<Long, ReputationVO> reputations) {
        return new PartyDetailResDTO(
                party.getId(),
                party.getTitle(),
                party.getContent(),
                party.getLeader().getId(),
                party.getLeader().getNickname(),
                party.getStatus(),
                party.getCreatedAt().toString(),
                party.getStartedAt() != null ? party.getStartedAt().toString() : null,
                party.getCompletedAt() != null ? party.getCompletedAt().toString() : null,
                // 슬롯 변환 시 리더 정보 및 평판 맵 전달
                party.getSlots().stream()
                        .map(slot -> SlotResDTO.of(slot, applicants, isLeader, reputations))
                        .toList(),
                links,
                myApplicantStatus
        );
    }
}
