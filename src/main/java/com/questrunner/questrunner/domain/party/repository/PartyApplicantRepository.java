package com.questrunner.questrunner.domain.party.repository;

import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyApplicantRepository extends JpaRepository<PartyApplicantEntity, Long> {

    // 중복 지원 방지: 해당 파티(슬롯의 파티)에 이미 지원했는지 확인
    boolean existsBySlot_Party_IdAndMember_Id(Long partyId, Long memberId);

    // 파티 ID 로 모든 지원자 조회 (N:1 관계 타고 조회)
    List<PartyApplicantEntity> findAllBySlot_Party_Id(Long partyId);
}
