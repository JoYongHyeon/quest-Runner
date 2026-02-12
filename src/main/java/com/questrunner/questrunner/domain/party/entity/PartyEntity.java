package com.questrunner.questrunner.domain.party.entity;

import com.questrunner.questrunner.domain.member.entity.MemberEntity;
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

    // 초대 링크 리스트
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyInviteLinkEntity> links = new ArrayList<>();


    // 생성자
    @Builder
    private PartyEntity(MemberEntity leader,
                        String title,
                        String content) {
        this.leader = leader;
        this.title = title;
        this.content = content;
        // 기본 값: 모집 중
        this.status = PartyStatus.RECRUITING;
    }


    /**
     * 파티에 새 슬롯 (모집 포지션)을 추가 (양방향 매핑)
     */
    public void addSlot(PartySlotEntity slot) {
        this.slots.add(slot);
        // 연관관계 편의 메서드
        slot.assignParty(this);
    }

    /**
     * 파티에 초대 링크를 추가 (양방향 매핑)
     */
    public void addLink(PartyInviteLinkEntity link) {
        this.links.add(link);
        link.assignParty(this);
    }

    /**
     * 기본 정보 수정
     */
    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 링크 전체 교체
     */
    public void replaceLinks(List<PartyInviteLinkEntity> newLinks) {
        this.links.clear();
        for (PartyInviteLinkEntity link : newLinks) {
            this.addLink(link);
        }
    }
}
