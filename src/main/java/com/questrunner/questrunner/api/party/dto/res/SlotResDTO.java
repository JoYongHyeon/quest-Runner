package com.questrunner.questrunner.api.party.dto.res;

import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.party.entity.PartySlotEntity;
import com.questrunner.questrunner.domain.party.vo.SlotStatus;

public record SlotResDTO(

        Long slotId,
        Position position,
        SlotStatus status
) {
    public static SlotResDTO from(PartySlotEntity slot) {
        return new SlotResDTO(slot.getId(), slot.getPosition(), slot.getStatus());
    }
}
