package com.sparta.actionboss.domain.user.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoDto {
    private Long id;
    private String nickname;
    private String email;

    public KakaoUserInfoDto(Long id, String nickname, String email){
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }

    public KakaoUserInfoDto(Long id, String nickname){
        this.id = id;
        this.nickname = nickname;
    }
}
//public record KakaoUserInfoDto(
//        Long id,
//        String nickname,
//        String email
//) {
//}
