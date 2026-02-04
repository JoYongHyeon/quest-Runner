package com.questrunner.questrunner.application.party;

import com.questrunner.questrunner.api.party.dto.req.PartyApplyReqDTO;
import com.questrunner.questrunner.api.party.dto.req.PartyCreateReqDTO;
import com.questrunner.questrunner.api.party.dto.req.PartySearchCondition;
import com.questrunner.questrunner.api.party.dto.res.PartyDetailResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyListResDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartyService {


    /**
     * 새로운 파티를 생성하고 모집 슬롯을 구성한다.
     * 무료 유저의 경우 1인 1파티 정책을 검증한다.
     *
     * @param leaderId 파티장(생성자) ID
     * @param req 파티 생성 정보 (제목, 내용, 슬롯, 구성)
     * @return 생성된 파티 ID
     */
    Long createParty(Long leaderId, PartyCreateReqDTO req);


    /**
     * 검색 조건에 맞는 파티 목록을 페이징하여 조회한다.
     * 모집 중 (RECRUITING) 인 파티만 조회되며, 최신순으로 정렬된다.
     *
     * @param condition 검색 필터 조건 (지역, 포지션 등 - null 일 경우 전체 조회)
     * @param pageable 페이징 정보 (page, size)
     * @return 검색된 파티 목록의 DTO 리스트 (Page 객체로 래핑됨)
     */
    Page<PartyListResDTO> getPartyList(PartySearchCondition condition, Pageable pageable);

    /**
     * 파티의 상세 정보를 조회 한다.
     * 파티장 정보와 슬롯 정보를 함계 반환합니다.
     *
     * @param partyId 조회할 파티 ID
     * @return 파티 상세 정보 DTO
     * @throws com.questrunner.questrunner.global.exception.BusinessException PARTY_NOT_FOUND
     */
    PartyDetailResDTO getPartyDetail(Long partyId);


    /**
     * 사용자가 특정 파티 슬롯에 지원합니다.
     *
     * @param memberId 지원자 ID
     * @param req 지원 정보 (슬롯 ID, 메시지)
     */
    void applyParty(Long memberId, PartyApplyReqDTO req);
}
