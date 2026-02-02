package com.questrunner.questrunner.domain.member.entity;

import com.questrunner.questrunner.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "member_tech_stack",
        indexes = @Index(name = "idx_member_tech_name", columnList = "member_id, tech_name", unique = true))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("회원 보유 기술 스택 (1:N)")
public class MemberTechStack extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_tech_stack_id")
    private Long id;

    @Comment("회원 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Comment("기술명 (예: Java, Spring, React)")
    @Column(name = "tech_name", nullable = false, length = 50)
    private String techName;

    // 생성자
    private MemberTechStack(MemberEntity member, String techName) {
        this.member = member;
        this.techName = techName;
    }

    public static MemberTechStack of(MemberEntity member, String techName) {
        return new MemberTechStack(member, techName);
    }
}
