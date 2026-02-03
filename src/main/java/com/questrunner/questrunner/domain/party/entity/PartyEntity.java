package com.questrunner.questrunner.domain.party.entity;

import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.member.vo.Region;
import com.questrunner.questrunner.domain.party.vo.PartyStatus;
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
@Table(name = "party")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("파티 (프로젝트 팀 모집 단위)")
public class PartyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "party_id")
    private Long id;

    @Comment("파티 제목")
    @Column(nullable = false, length = 100)
    private String title;

    @Comment("파티 상세 내용")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Comment("모집 지역 (온라인/서울/경기 등)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Region region;

    @Comment("파티 상태 (RECRUITING, COMPLETED...)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartyStatus status;


    // 파티장(리더) - N:1
    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    @Comment("파티장 (Memeber FK")
    private MemberEntity leader;

    // 파티 슬롯 목록 (1:N)
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartySlotEntity> slots = new ArrayList<>();


    @Builder
    private PartyEntity(MemberEntity leader,
                        String title,
                        String content,
                        Region region) {
        this.leader = leader;
        this.title = title;
        this.content = content;
        this.region = region;
        // 기본 값: 모집 중
        this.status = PartyStatus.RECRUITING;
    }

    // --- 비즈니스 메서드 ---

    /**
     * 파티에 새 슬롯 (모집 포지션)을 추가한다.
     */
    public void addSlot(PartySlotEntity slot) {
        this.slots.add(slot);
        // 연관관계 편의 메서드
        slot.assignParty(this);
    }
}
