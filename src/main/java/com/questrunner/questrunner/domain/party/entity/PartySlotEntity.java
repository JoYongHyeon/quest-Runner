package com.questrunner.questrunner.domain.party.entity;

import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.party.vo.SlotStatus;
import com.questrunner.questrunner.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "party_slot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("파티 내 모집 슬롯 (자리)")
public class PartySlotEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "party_slot_id")
    private Long id;

    @Comment("모집 포지션 (BACKEND, DESIGN...)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Position position;

    @Comment("슬롯 상태 (OPEN, LOCKED)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotStatus status;

    //소속 파티
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private PartyEntity party;

    @Builder
    private PartySlotEntity(Position position) {
        this.position = position;
        // 기본 값 : 비어있음
        this.status = SlotStatus.OPEN;
    }

    // 연관관계 설정용 (PartyEntity 에서 호출)
    public void assignParty(PartyEntity party) {
        this.party = party;
    }

    public void lock() {
        this.status = SlotStatus.LOCKED;
    }
}
