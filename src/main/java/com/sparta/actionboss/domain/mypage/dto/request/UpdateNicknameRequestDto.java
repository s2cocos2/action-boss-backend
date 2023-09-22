package com.sparta.actionboss.domain.mypage.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdateNicknameRequestDto(
        @Pattern(regexp = "^(?!.*\\s)[a-zA-Z0-9가-힣]{2,15}$", message = "올바른 닉네임 형식이 아닙니다.")
        String nickname
) {
}
