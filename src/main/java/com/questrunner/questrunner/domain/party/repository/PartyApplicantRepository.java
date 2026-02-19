package com.questrunner.questrunner.domain.party.repository;

import com.questrunner.questrunner.domain.party.entity.PartyApplicantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartyApplicantRepository extends JpaRepository<PartyApplicantEntity, Long> {

    // 중복 지원 방지: 해당 파티(슬롯의 파티)에 이미 지원했는지 확인
    boolean existsBySlot_Party_IdAndMember_Id(Long partyId, Long memberId);

    // 파티 ID 로 모든 지원자 조회 (N:1 관계 타고 조회)
    List<PartyApplicantEntity> findAllBySlot_Party_Id(Long partyId);

    // 슬롯 삭제 시 해당 슬롯의 지원자 일괄 삭제 (파티 수정 OPEN 슬롯 교체용)
    void deleteAllBySlot_Id(Long slotId);

    /**
     * 특정 회원이 지원한 모든 내역을 조회한다. (최신 순)
     * N+1 문제를 방지하기 위해 Slot, Party, Leader 정보를 Fetch Join 으로 함께 가져옵니다.
     */
    @Query("SELECT pa FROM PartyApplicantEntity pa " +
            "JOIN FETCH pa.slot s " +
            "JOIN FETCH s.party p " +
            "JOIN FETCH p.leader " +
            "WHERE pa.member.id = :memberId " +
            "ORDER BY pa.createdAt DESC")
    List<PartyApplicantEntity> findAllByMemberId(@Param("memberId") Long memberId);

    // 파티 + 회원으로 지원 내역 단건 조회 (상태 확인용)
    Optional<PartyApplicantEntity> findBySlot_Party_IdAndMember_Id(Long slotPartyId, Long memberId);
}
