package com.questrunner.questrunner.domain.party.repository;

import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyApplicantRepository extends JpaRepository<PartyApplicantEntity, Long> {

    // 중복 지원 방지: 해당 파티(슬롯의 파티)에 이미 지원했는지 확인
    boolean existsBySlot_Party_IdAndMember_Id(Long partyid, Long memberId);
}
