package com.sparta.actionboss.domain.mypage.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyPageInfoResponseDto {
    private String email;
    private String nickname;

    public MyPageInfoResponseDto(String email, String nickname){
        this.email = email;
        this.nickname = nickname;
    }
}
