package com.questrunner.questrunner.domain.party.repository;

import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartyRepository extends JpaRepository<PartyEntity, Long>, PartyRepositoryCustom {

    /**
     * 무료 유저 1파티 생성 제한용
     * - 리더 + 현재 모집 중인 (RECRUITING) 파티가 있는지 확인
     */
    boolean existsByLeaderIdAndStatus(Long leaderId, PartyStatus status);

}
