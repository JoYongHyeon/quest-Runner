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
@Table(name = "party_invite_link")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("파티 멤버 전용 초대 링크")
public class PartyInviteLinkEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long id;

    @Comment("링크 라벨 (예: 디스스코드, 노션)")
    @Column(nullable = false, length = 50)
    private String label;

    @Comment("초대 URL")
    @Column(nullable = false, length = 500)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private PartyEntity party;

    // 생성자
    @Builder
    private PartyInviteLinkEntity(String label, String url) {
        this.label = label;
        this.url = url;
    }

     // 연관관계 편의 메서드 - 파티 할당
    public void assignParty(PartyEntity party) {
        this.party = party;
    }
}
