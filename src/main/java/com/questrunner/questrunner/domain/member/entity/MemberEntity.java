package com.questrunner.questrunner.domain.member.entity;

import com.questrunner.questrunner.domain.member.vo.Position;
import com.questrunner.questrunner.domain.member.vo.Region;
import com.questrunner.questrunner.domain.member.vo.UserRole;
import com.questrunner.questrunner.domain.member.vo.UserStatus;
import com.questrunner.questrunner.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("회원 정보 (가입 및 프로필)")
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    // --- 인증 필수 정보 (Authentication) ---
    @Comment("구글 이메일 (식별자)")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Comment("제공자 (google)")
    @Column(nullable = false, length = 20)
    private String provider;

    @Comment("제공자 내부 ID")
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Comment("권한")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Comment("계정 상태: PENDING_PROFILE(가입 직후) -> ACTIVE(온보딩 완료)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "last_login_at", nullable = false)
    private LocalDateTime lastLoginAt;

    // --- 프로필 정보 (Onboarding 과정에서 입력 null 허용) ---
    @Comment("사용자 이름 (닉네임)")
    @Column(name = "nick_name", length = 50)
    private String nickname;

    @Comment("포지션")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Position position;

    @Comment("활동 지역")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Region region;

    @Comment("한줄 소개")
    @Column(length = 255)
    private String intro;

    // --- 링크 정보(옵션) ---
    @Comment("대표 링크 (GitHub)")
    @Column(name = "git_url", length = 500)
    private String gitUrl;

    @Comment("블로그 링크 (Notion/Tistory)")
    @Column(name = "blog_url", length = 500)
    private String blogUrl;

    @Comment("이력서/포트폴리오 링크")
    @Column(name = "resume_link", length = 500)
    private String resumeLink;


    // TODO: 추후 Skin 엔티티와 연관관계 맺을 예정 (현재는 ID만 저장)
//    @Comment("스킨 ID")
//    @Column(name = "skin_id", nullable = false)
//    private Long skinId;


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberTechStackEntity> techStacks = new ArrayList<>();

    @Builder
    private MemberEntity(String email,
                         String nickname,
                         String provider,
                         String providerId,
                         UserRole role,
                         UserStatus status,
//                         Long skinId,
                         LocalDateTime lastLoginAt) {
        this.email       = email;
        this.nickname    = nickname;
        this.provider    = provider;
        this.providerId  = providerId;
        this.role        = role;
        this.status      = status;
//        this.skinId      = skinId;
        this.lastLoginAt = lastLoginAt;
    }

    // 로그인 성공 시 마지막 접속 시간을 업데이트
    public void completeLoginSession() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * - 회원가입 후 온보딩(프로필 입력)을 수행
     * - 필수 정보가 입력되면 계정 상태를 "ACTIVE" 로 변경하여 활동을 허용
     */
    public void updateOnboardingProfile(String nickname,
                                        Position position,
                                        Region region,
                                        String intro,
                                        String gitUrl,
                                        String blogUrl,
                                        String resumeLink) {
        this.nickname = nickname;
        this.position = position;
        this.region = region;
        this.intro = intro;
        this.gitUrl = gitUrl;
        this.blogUrl = blogUrl;
        this.resumeLink = resumeLink;

        // 프로필 입력 완료 시 상태 변경
        if (this.status == UserStatus.PENDING_PROFILE) {
            this.status = UserStatus.ACTIVE;
        }
    }


    // --- 연관관계 편의 메서드 ---
    public void addTechStack(String techName) {
        MemberTechStackEntity techStack = MemberTechStackEntity.of(this, techName);
        this.techStacks.add(techStack);
    }
    
    // 기술 스택 전체 초기화 (수정 시 사용)
    public void clearTechStacks() {
        this.techStacks.clear();
    }
}
