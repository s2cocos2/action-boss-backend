package com.sparta.actionboss.global.filter;

import com.sparta.actionboss.global.security.UserDetailsService;
import com.sparta.actionboss.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static com.sparta.actionboss.global.util.JwtUtil.AUTHORIZATION_ACCESS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

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
            //given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();
            String accessToken = "accessToken";
            request.addHeader(AUTHORIZATION_ACCESS, accessToken);

            given(jwtUtill.getJwtFromHeader(request, AUTHORIZATION_ACCESS)).willReturn(accessToken);
            given(jwtUtill.validateAccessToken(accessToken)).willReturn(true);

            Claims claims = mock(Claims.class);
            given(jwtUtill.getUserInfoFromAccessToken(accessToken)).willReturn(claims);
            given(claims.getSubject()).willReturn("1");

            given(userDetailsService.loadUserByUserId(1L)).willReturn(mock(UserDetails.class));

            //when
            jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

            //then
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            reset(jwtUtill);
        }
        @Test
        @DisplayName("filter 인증 실패 - token error")
        void doFilterInternalFail() throws ServletException, IOException {
            //given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();
            String accessToken = "accessToken";
            request.addHeader(AUTHORIZATION_ACCESS, accessToken);

            given(jwtUtill.getJwtFromHeader(request, AUTHORIZATION_ACCESS)).willReturn(accessToken);
            given(jwtUtill.validateAccessToken(accessToken)).willReturn(false);

            //when
            jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

            //then
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Test
    @DisplayName("filter 인증 실패 - wrong subject")
    void doFilterInternalFail() throws ServletException, IOException {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        String accessToken = "accessToken";
        request.addHeader(AUTHORIZATION_ACCESS, accessToken);

        given(jwtUtill.getJwtFromHeader(request, AUTHORIZATION_ACCESS)).willReturn(accessToken);
        given(jwtUtill.validateAccessToken(accessToken)).willReturn(true);

        Claims claims = mock(Claims.class);
        given(jwtUtill.getUserInfoFromAccessToken(accessToken)).willReturn(claims);
        given(claims.getSubject()).willReturn("wrong subject");

        //when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        //then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}