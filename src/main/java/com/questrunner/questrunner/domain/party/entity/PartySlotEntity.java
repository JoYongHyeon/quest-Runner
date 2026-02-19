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

import java.util.ArrayList;
import java.util.List;

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

    // 기술 스택 리스트
    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartySlotTechEntity> techStacks = new ArrayList<>();

    // 생성자
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

    /**
     * 슬롯에 요구 기술 스택을 추가 - (양방향 매핑)
     */
    public void addTechStack(PartySlotTechEntity techStack) {
        this.techStacks.add(techStack);
        techStack.assignSlot(this);
    }

    /**
     * 슬롯 포지션 수정 메서드
     */
    public void updatePosition(Position position) {
        this.position = position;
    }

    public void lock() {
        this.status = SlotStatus.LOCKED;
    }

    /**
     * 슬롯을 다시 모집 중 상태로 변경 (추방 시 호출)
     */
    public void open() {
        this.status = SlotStatus.OPEN;
    }
}
