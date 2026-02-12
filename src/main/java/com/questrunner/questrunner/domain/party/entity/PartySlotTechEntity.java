package com.questrunner.questrunner.domain.party.entity;

import com.questrunner.questrunner.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "party_slot_tech")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("파티 슬롯별 요구 기술 스택")
public class PartySlotTechEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_tech_id")
    private Long id;

    @Comment("요구 기술명 (예: Java, Vue.js)")
    @Column(nullable = false, length = 50)
    private String techName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_slot_id", nullable = false)
    private PartySlotEntity slot;

    // 생성자
    @Builder
    private PartySlotTechEntity(String techName) {
        this.techName = techName;
    }

    // 연관관계 편의 메서드 - 슬롯 할당
    public void assignSlot(PartySlotEntity slot) {
        this.slot = slot;
    }
}
