package com.sparta.actionboss.domain.user.service;

import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KakaoIdUpdateTest {

    @Test
    @DisplayName("kakaoId 업데이트")
    void kakaoIdUpdate(){
        User user = new User("코코","abcd1234","coco@naver.com", UserRoleEnum.USER, 12345L);
        Long newKakaoId = 654321L;

        user.kakaoIdUpdate(newKakaoId);

        assertEquals(newKakaoId, user.getKakaoId());

    }

}
