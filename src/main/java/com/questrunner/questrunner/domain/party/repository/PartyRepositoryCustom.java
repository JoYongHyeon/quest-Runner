package com.questrunner.questrunner.domain.party.repository;

import com.questrunner.questrunner.api.party.dto.req.PartySearchCondition;
import com.questrunner.questrunner.domain.party.entity.PartyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PartyRepositoryCustom {


    /**
     * 파티 목록을 검색 조건에 따라 페이징 조회
     */
    Page<PartyEntity> searchParties(PartySearchCondition condition, Pageable pageable);


    // 파티 상세 조회 용
    Optional<PartyEntity> findByIdWithAll(Long partyId);
}
