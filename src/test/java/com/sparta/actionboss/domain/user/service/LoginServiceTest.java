package com.sparta.actionboss.domain.user.service;

import com.sparta.actionboss.domain.user.dto.request.LoginRequestDto;
import com.sparta.actionboss.domain.user.entity.RefreshToken;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.repository.RefreshTokenRepository;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.NestedTestConfiguration;

import java.util.Optional;

import static com.sparta.actionboss.global.util.JwtUtil.AUTHORIZATION_REFRESH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtUtil jwtUtil;

    LoginService loginService;

    @BeforeEach
    void setUp(){
        loginService = new LoginService(userRepository, refreshTokenRepository, passwordEncoder, jwtUtil);
    }

    @Nested
    @DisplayName("로그인")
    class login{

        @Test
        @DisplayName("로그인 성공")
        void loginSuccess(){
            //given
            LoginRequestDto requestDto = new LoginRequestDto("coco@naver.com", "abcd1234");
            MockHttpServletResponse response = new MockHttpServletResponse();
            Long userId = 1L;
            String nickname = "코코";
            String password = passwordEncoder.encode("abcd1234");
            String email = "coco@naver.com";
            User user = new User(userId, nickname, password, email, UserRoleEnum.USER, null);

            given(userRepository.findByEmail(requestDto.email())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(requestDto.password(), user.getPassword())).willReturn(true);
            given(jwtUtil.createAccessToken(userId,user.getRole())).willReturn("accessToken");
            given(jwtUtil.createRefreshToken(userId)).willReturn("refreshToken");

            //when
            CommonResponse result = loginService.login(requestDto, response);

            //then
            assertEquals("로그인에 성공하였습니다.", result.getMsg());
        }

        @Test
        @DisplayName("로그인 실패 - 가입되지 않은 계정")
        void loginFail1(){
            //given
            LoginRequestDto requestDto = new LoginRequestDto("coco@naver.com", "abcd1234");
            MockHttpServletResponse response = new MockHttpServletResponse();
            given(userRepository.findByEmail(requestDto.email())).willReturn(Optional.empty());

            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                loginService.login(requestDto, response);
            });

            //then
            assertEquals("가입되지 않은 계정입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호가 틀렸을 때")
        void loginFai2l(){
            //given
            LoginRequestDto requestDto = new LoginRequestDto("coco@naver.com", "abcd12345");
            MockHttpServletResponse response = new MockHttpServletResponse();
            String nickname = "코코";
            String password = passwordEncoder.encode("abcd1234");
            String email = "coco@naver.com";
            User user = new User(nickname, password, email, UserRoleEnum.USER);

            given(userRepository.findByEmail(requestDto.email())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(requestDto.password(), user.getPassword())).willReturn(false);
            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                loginService.login(requestDto, response);
            });

            //then
            assertEquals("잘못된 비밀번호 입니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class reissueToken{

        @Test
        @DisplayName("토큰 재발급 성공")
        void reissueTokenSuccess(){
            //given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            String refreshToken = "refreshToken";
            Long userId = 1L;

            given(jwtUtil.getJwtFromHeader(request,AUTHORIZATION_REFRESH)).willReturn(refreshToken);
            given(jwtUtil.validateRefreshToken(refreshToken)).willReturn(true);
            given(refreshTokenRepository.findByRefreshToken(refreshToken)).willReturn(Optional.of(new RefreshToken()));
            given(jwtUtil.getUserInfoFromRefreshToken(refreshToken)).willReturn(userId);
            given(jwtUtil.createAccessToken(userId, UserRoleEnum.USER)).willReturn("new accessToken");

            //when
            CommonResponse commonResponse = loginService.reissueToken(request, response);

            //then
            assertEquals("Access Token이 재발급되었습니다.", commonResponse.getMsg());
        }

        @Test
        @DisplayName("토큰 재발급 실패 - refreshToken이 존재하지 않을 때 ")
        void reissueTokenFail1(){
            //given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            String refreshToken = "refreshToken";

            given(jwtUtil.getJwtFromHeader(request,AUTHORIZATION_REFRESH)).willReturn(refreshToken);
            given(jwtUtil.validateRefreshToken(refreshToken)).willReturn(true);
            given(refreshTokenRepository.findByRefreshToken(refreshToken)).willReturn(Optional.empty());

            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                loginService.reissueToken(request, response);
            });

            //then
            assertEquals("refresh token이 존재하지 않습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("토큰 재발급 실패 - refreshToken이 유효하지 않을 때 ")
        void reissueTokenFail2(){
            //given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            String refreshToken = "refreshToken";

            given(jwtUtil.getJwtFromHeader(request,AUTHORIZATION_REFRESH)).willReturn(refreshToken);
            given(jwtUtil.validateRefreshToken(refreshToken)).willReturn(false);

            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                loginService.reissueToken(request, response);
            });

            //then
            assertEquals("유효하지 않은 refresh token 입니다.", exception.getMessage());
        }
    }
}