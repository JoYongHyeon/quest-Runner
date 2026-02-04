package com.questrunner.questrunner.api.party.dto.req;

import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.member.vo.Region;

public record PartySearchCondition(

        Region region,
        Position position
) {}
