package com.sparta.actionboss.global.filter;

import com.sparta.actionboss.global.security.UserDetailsImpl;
import com.sparta.actionboss.global.security.UserDetailsService;
import com.sparta.actionboss.global.util.JwtUtil;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static com.sparta.actionboss.global.util.JwtUtil.AUTHORIZATION_ACCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtAuthorizationFilterTest {

    @Mock
    JwtUtil jwtUtill;

    @Mock
    UserDetailsService userDetailsService;

    JwtAuthorizationFilter jwtAuthorizationFilter;

    @BeforeEach
    void setUp(){
        jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtUtill, userDetailsService);
    }

    @Nested
    @DisplayName("filter 인증")
    class doFilterInternal{
        @Test
        @DisplayName("filter 인증 성공")
        void doFilterInternalSuccess() throws ServletException, IOException {
            //when
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            String accessToken = "accessToken";
            given(jwtUtill.getJwtFromHeader(request, AUTHORIZATION_ACCESS));
            given(jwtUtill.validateAccessToken(accessToken)).willReturn(false);

            //given


            //then
        }
        @Test
        @DisplayName("filter 인증 실패")
        void doFilterInternalFail() throws ServletException, IOException {
            //when
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            String accessToken = "accessToken";
            given(jwtUtill.getJwtFromHeader(request, AUTHORIZATION_ACCESS));
            given(jwtUtill.validateAccessToken(accessToken)).willReturn(false);

            //given



            //then
        }
    }

}