package com.questrunner.questrunner.api.member.dto.req;

import com.questrunner.questrunner.domain.member.vo.Position;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MemberProfileReqDTO(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
        String nickname,

        @NotNull(message = "포지션은 필수입니다.")
        Position position,

        // 한줄 소개
        @Size(max = 1000, message = "자기 소개는 1000자 이내로 작성해주세요.")
        String intro,
        // 대표 링크
        String gitUrl,
        // 블로그 링크
        String blogUrl,
        // 이력서/포트폴리오 링크
        String resumeLink,

        @NotNull(message = "기술 스택 리스트는 null일 수 없습니다.")
        List<String> techStacks
) {}
