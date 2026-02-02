package com.questrunner.questrunner.domain.member.vo;

import lombok.Getter;

@Getter
public enum UserStatus {

    // 정상 계정 : 로그인 및 주문 가능
    ACTIVE {
        public boolean canLogin() {return true;}
    },
    // 프로필 미입력 계정 (로그인 가능, 활동 불가능)
    PENDING_PROFILE {
        public boolean canLogin() {return true;}
    },
    // 차단 계정 : 로그인 및 주문 모두 불가
    BLOCKED {
        public boolean canLogin() {return false;}

    },
    // 탈퇴 계정 : 모든 행위 불가 (이력 보존용)
    WITHDRAWN{
        public boolean canLogin() {return false;}
    };

    // 로그인 가능 여부
    public abstract boolean canLogin();

}
