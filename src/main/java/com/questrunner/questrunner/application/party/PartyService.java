package com.questrunner.questrunner.application.party;

import com.questrunner.questrunner.api.party.dto.req.*;
import com.questrunner.questrunner.api.party.dto.res.PartyApplicantResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyApplicationListResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyDetailResDTO;
import com.questrunner.questrunner.api.party.dto.res.PartyListResDTO;
import com.questrunner.questrunner.global.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
    PartyDetailResDTO getPartyDetail(Long memberId, Long partyId);


    /**
     * 사용자가 특정 파티 슬롯에 지원합니다.
     *
     * @param memberId 지원자 ID
     * @param req 지원 정보 (슬롯 ID, 메시지)
     */
    void applyParty(Long memberId, PartyApplyReqDTO req);


    /**
     * 특정 파티의 지원자 목록을 조회합니다. (파티장 전용)
     *
     * @param leaderId 요청한 회원 (파티장) ID
     * @param partyId partyId 파티 ID
     * @return 지원자 목록 DTO 리스트
     */
    List<PartyApplicantResDTO> getApplicants(Long leaderId, Long partyId);


    /**
     * 지원자를 수락하거나 거절합니다.
     * 수락 시 해당 슬롯은 장금 (LOCKED) 처리됩니다.
     *
     * @param leaderId 요청한 회원(파티장) ID
     * @param applicantId applicantId 지원 내역 ID
     * @param req 결정 상태 (ACCEPTED/REJECTED)
     */
    void decideApplicant(Long leaderId, Long applicantId, ApplicantDecisionReqDTO req);


    /**
     * 내가 생성한 파티 목록을 조회한다.
     *
     * @param memberId 회원 ID (파티장)
     * @return 파티 목록 DTO
     */
    List<PartyListResDTO> getMyParties(Long memberId);


    /**
     * 파티 정보를 수정합니다.
     * - LOCKED(채용 확정) 된 슬롯은 삭제/수정할 수 없습니다.
     * - OPEN(모집 중) 인 슬롯은 요청된 정보로 전면 교체됩니다.
     */
    void updateParty(Long leaderId, Long partyId, PartyUpdateReqDTO req);

    /**
     * 내가 지원한 파티(퀘스트) 목록을 조회 합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 지원한 파티 목록 DTO 리스트
     */
    List<PartyApplicationListResDTO> getMyAppliedParties(Long memberId);

    /**
     * 지원자가 승인 전 (PENDING) 상태에서 지원을 취소합니다.
     * - DB 에서 지원 내역을 완전히 삭제합니다. (기록 x)
     *
     * @param memberId 요청한 회원(지원자) ID
     * @param applicantId 취소할 지원 내역 ID
     */
    void cancelApplication(Long memberId, Long applicantId);

    /**
     * 지원자가 승인 후 (ACCEPTED) 상태에서 파티를 탈퇴합니다.
     * - DB 내역을 보존하고 상태를 QUIT(중도 탈퇴)으로 변경합니다.
     *
     * @param memberId 요청한 회원(지원자) ID
     * @param applicantId 탈퇴할 지원 내역 ID
     */
    void quitParty(Long memberId, Long applicantId);

    /**
     * 파티장이 특정 지원자를 강제 추방합니다.
     * - 지원자 상태를 KICKED 로 변경하고, 사유를 기록합니다.
     * - 해당 지원자가 차지하고 있던 슬롯(LOCKED)은 다시 OPEN 상태로 변경되어 모집을 재개합니다.
     *
     * @param leaderId 요청한 파티장 ID
     * @param applicantId 추방할 지원 내역 ID
     * @param req 추방 사유가 담긴 DTO
     */
    void kickApplicant(Long leaderId, Long applicantId, PartyKickReqDTO req);

    /**
     * 퀘스트를 공식적으로 시작합니다. (RECRUITING -> IN_PROGRESS)
     * @param leaderId 요청한 리더 ID
     * @param partyId 시작할 파티 ID
     * @throws BusinessException NOT_ENOUGH_MEMBERS (팀원이 없을 경우)
     */
    void startQuest(Long leaderId, Long partyId);

    /**
     * 퀘스트를 성공적으로 완료 처리합니다. (IN_PROGRESS -> COMPLETED)
     * @param leaderId 요청한 리더 ID
     * @param partyId 완료할 파티 ID
     */
    void completeQuest(Long leaderId, Long partyId);

    /**
     * 파티 모집을 취소합니다. (파티장 전용)
     * @param leaderId 요청한 파티장 ID
     * @param partyId 취소할 파티 ID
     */
    void cancelParty(Long leaderId, Long partyId);
}
