package com.sparta.actionboss.global.util;

import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    JwtUtil jwtUtil;

    @BeforeEach
    void setUp(){
        String accessTokenSecretKey = "accessTokenSecretKey";
        String refreshTokenSecretKey = "refreshTokenSecretKey";
        jwtUtil = new JwtUtil();
        jwtUtil.init();
//        String accessToken = jwtUtil.createAccessToken(1L, UserRoleEnum.USER);
//        String refresToken = jwtUtil.createRefreshToken(1L);
    }

    @Test
    @DisplayName("accessToken 생성")
    void createAccessToken(){
        //when
        Long userId = 1L;
        UserRoleEnum role = UserRoleEnum.USER;


        //given
        String accessToken = jwtUtil.createAccessToken(userId, role);

        //then
        assertNotNull(accessToken);
    }

    //when


    //given


    //then


}