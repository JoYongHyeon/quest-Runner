package com.questrunner.questrunner.domain.party.entity;

import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;
import com.questrunner.questrunner.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "party_applicant",
        uniqueConstraints = @UniqueConstraint(columnNames = { "slot_id", "member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("파티 지원 내역")
public class PartyApplicantEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicant_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicantStatus status;

    @Column(length = 500)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private PartySlotEntity slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Builder
    private PartyApplicantEntity(PartySlotEntity slot, MemberEntity member, String message) {
        this.slot = slot;
        this.member = member;
        this.message = message;
        this.status = ApplicantStatus.PENDING;
    }


    public void accept() {
        this.status = ApplicantStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ApplicantStatus.REJECTED;
    }
}
