package com.sparta.actionboss.domain.user.dto.request;

import jakarta.validation.constraints.Email;

public record CheckEmailRequestDto(
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,
        String successKey
) {
}
