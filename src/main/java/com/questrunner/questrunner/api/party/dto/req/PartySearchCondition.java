package com.questrunner.questrunner.api.party.dto.req;

import com.questrunner.questrunner.domain.member.vo.Position;

public record PartySearchCondition(

        Position position
) {}
