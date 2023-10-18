package com.sparta.actionboss.global.security;

import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.global.exception.CommonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

    @Mock
    UserRepository userRepository;

    UserDetailsService userDetailsService;

    @BeforeEach
    void setUp(){
        userDetailsService = new UserDetailsService(userRepository);
    }

    @Test
    @DisplayName("userId로 user찾기 - 성공")
    void loadUserByUserIdSuccess() throws CommonException {
        //given
        Long userId = 1l;
        User user = new User();
        given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));

        //when
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUserId(userId);

        //then
        assertEquals(user, userDetails.getUser());
    }

    @Test
    @DisplayName("userId로 user찾기 - 실패")
    void loadUserByUserIdFail() throws CommonException {
        //given
        Long userId = 1l;
        given(userRepository.findByUserId(userId)).willReturn(Optional.empty());

        //when
        Exception exception = assertThrows(CommonException.class, ()->{
            userDetailsService.loadUserByUserId(userId);
        });

        //then
        assertEquals("가입되지 않은 계정입니다.", exception.getMessage());
    }

}