package com.sparta.actionboss.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class SendEmailRequestDto {
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}
