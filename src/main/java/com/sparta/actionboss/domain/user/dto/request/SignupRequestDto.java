package com.sparta.actionboss.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

//@Getter
//public class SignupRequestDto {
//    @Email(message = "올바른 이메일 형식이 아닙니다.")
//    private String email;
//    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,15}$")
//    private String password;
//    @Pattern(regexp = "^(?!.*\\s)[a-zA-Z0-9가-힣]{2,15}$")
//    private String nickname;
//    private String successKey;
//    private boolean admin = false;
//    private String adminToken = "";
//}

@Getter
public class SignupRequestDto {
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private final String email;
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,15}$", message = "올바른 비밀번호 형식이 아닙니다.")
    private final String password;
    @Pattern(regexp = "^(?!.*\\s)[a-zA-Z0-9가-힣]{2,15}$", message = "올바른 닉네임 형식이 아닙니다.")
    private final String nickname;
    private final String successKey;
    private boolean admin = false;
    private String adminToken = "";

    public SignupRequestDto(String email, String password, String nickname, String successKey) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.successKey = successKey;
    }

    public SignupRequestDto(String email, String password, String nickname, String successKey, boolean admin, String adminToken) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.successKey = successKey;
        this.admin = admin;
        this.adminToken = adminToken;
    }
}
