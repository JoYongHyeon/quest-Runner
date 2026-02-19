package com.questrunner.questrunner.domain.party.entity;

import com.questrunner.questrunner.domain.member.entity.MemberEntity;
import com.questrunner.questrunner.domain.party.vo.ApplicantStatus;
import com.questrunner.questrunner.global.entity.BaseEntity;
import com.questrunner.questrunner.global.enums.ErrorCode;
import com.questrunner.questrunner.global.exception.BusinessException;
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

    @Column(length = 500)
    @Comment("상태 변경 사유 (예: 강제 추방 사유)")
    private String changeReason;

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


    // 승인
    public void accept() {
        this.status = ApplicantStatus.ACCEPTED;
    }

    // 거절
    public void reject() {
        this.status = ApplicantStatus.REJECTED;
    }

    /**
     * 지원자 본인이 맞는지 검증
     * @param memberId 요청한 회원의 ID
     * @throws BusinessException 본인이 아닐 경우 (NOT_MY_APPLICATION)
     */
    public void validateOwner(Long memberId) {
        if (!this.member.getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.NOT_MY_APPLICATION);
        }
    }

    /**
     * 자진 탈퇴
     * - 상태를 QUIT 으로 변경
     */
    public void quit() {
        // 승인된 상태가 아니면 탈퇴 불가 (취소 이용 유도)
        if (this.status != ApplicantStatus.ACCEPTED) {
            throw new BusinessException(ErrorCode.CANNOT_QUIT_NOT_ACCEPTED);
        }
        this.status = ApplicantStatus.QUIT;
    }

    /**
     * 강제 추방
     * - 상태를 KICKED 로 변경하고 사유 기록
     */
    public void kick(String reason) {
        this.status = ApplicantStatus.KICKED;
        this.changeReason = reason;
    }

}
