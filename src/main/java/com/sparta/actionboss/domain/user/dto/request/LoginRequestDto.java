package com.sparta.actionboss.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

public record LoginRequestDto(
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,15}$", message = "올바른 비밀번호 형식이 아닙니다.")
        String password
) {
}