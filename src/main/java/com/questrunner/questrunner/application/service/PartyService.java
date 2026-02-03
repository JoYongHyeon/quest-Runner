package com.questrunner.questrunner.application.service;

import com.questrunner.questrunner.api.party.dto.req.PartyCreateReqDTO;

public interface PartyService {


    /**
     * 새로운 파티를 생성한다.
     *
     * @param leaderId 파티장(생성자) ID
     * @param req 파티 생성 정보 (제목, 내용, 슬롯, 구성)
     * @return 생성된 파티 ID
     */
    Long createParty(Long leaderId, PartyCreateReqDTO req);
}
