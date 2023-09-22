package com.sparta.actionboss.domain.mypage.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdatePasswordRequestDto(
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,15}$", message = "올바른 비밀번호 형식이 아닙니다.")
        String password
) {
}