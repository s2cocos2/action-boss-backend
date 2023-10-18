package com.sparta.actionboss.global.util;

import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import static com.sparta.actionboss.global.util.JwtUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Autowired JwtUtil jwtUtil;

    @Test
    @DisplayName("accessToken 생성")
    void createAccessToken(){
        Long userId = 1L;
        UserRoleEnum role = UserRoleEnum.USER;

        String accessToken = jwtUtil.createAccessToken(userId, role);

        assertNotEquals(accessToken, BEARER_PREFIX);
    }

    @Test
    @DisplayName("refreshToken 생성")
    void createRefreshToken(){
        Long userId = 1L;

        String refreshToken = jwtUtil.createRefreshToken(userId);

        assertNotEquals(refreshToken, BEARER_PREFIX);
    }

    @Nested
    @DisplayName("헤더에서 JWT가져오기")
    class getJwtFromHeader{
        @Test
        @DisplayName("헤더에서 JWT 가져오기 - accessToken 일때")
        void getAccessJwtFromHeader(){
            //given
            MockHttpServletRequest request = new MockHttpServletRequest();
            String tokenType = "Access";
            String bearerToken = BEARER_PREFIX + "token";
            request.addHeader(AUTHORIZATION_ACCESS, bearerToken);

            //when
            String token = jwtUtil.getJwtFromHeader(request, tokenType);

            //then
            assertEquals("token", token);
        }

        @Test
        @DisplayName("헤더에서 JWT 가져오기 - refreshToken 일때")
        void getRefreshJwtFromHeader(){
            //given
            MockHttpServletRequest request = new MockHttpServletRequest();
            String tokenType = "Refresh";
            String bearerToken = BEARER_PREFIX + "token";
            request.addHeader(AUTHORIZATION_REFRESH, bearerToken);

            //when
            String token = jwtUtil.getJwtFromHeader(request, tokenType);

            //then
            assertEquals("token", token);
        }

        @Test
        @DisplayName("헤더에서 JWT 가져오기 실패")
        void getJwtFromHeader(){
            MockHttpServletRequest request = new MockHttpServletRequest();
            String tokenType = " ";

            String token = jwtUtil.getJwtFromHeader(request, tokenType);

            assertNull(token);

        }
    }

    @Test
    @DisplayName("accessToken 유효성 검사 성공")
    void validateAccessTokenSuccess(){
        String bearerToken = jwtUtil.createAccessToken(1L,UserRoleEnum.USER);
        String token = bearerToken.replace(BEARER_PREFIX, " ");

        boolean validToken = jwtUtil.validateAccessToken(token);

        assertTrue(validToken);
    }

    @Nested
    @DisplayName("accessToken 유효성 검사 실패")
    class validateAccessTokenFail{
        @Test
        @DisplayName("accessToken 유효성 검사 실패 - 유효하지 않은 JWT")
        void validateAccessTokenFail1(){
            String token = "wrong token";
            assertThrows(IllegalArgumentException.class,()->{
                jwtUtil.validateAccessToken(token);
            });
        }

        @Test
        @DisplayName("accessToken 유효성 검사 실패 - 만료된 JWT")
        void validateAccessTokenFail2(){
            String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0IiwiYXV0aCI6IlVTRVIiLCJleHAiOjE2OTU1MzQ0OTksImlhdCI6MTY5NTUzMDg5OX0.CouyX_QtF8Sy9E1Iin2NmsJmubRgNQ2OuGSGjNMZZ2g";
            assertThrows(IllegalArgumentException.class,()->{
                jwtUtil.validateAccessToken(token);
            });
        }

        @Test
        @DisplayName("accessToken 유효성 검사 실패 - 지원되지 않는 JWT")
        void validateAccessTokenFail3(){
            String token = "eyJhbGciOiJOT05FIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.DjwRE2RJ21X_p-BzUI1OlYh8i4SKLIZryRrRPcXG5Bc";
            assertThrows(IllegalArgumentException.class,()->{
                jwtUtil.validateAccessToken(token);
            });
        }

        @Test
        @DisplayName("accessToken 유효성 검사 실패 - 잘못된 JWT")
        void validateAccessTokenFail4(){
            String token = " ";
            assertThrows(IllegalArgumentException.class,()->{
                jwtUtil.validateAccessToken(token);
            });
        }
    }

    @Test
    @DisplayName("refreshToken 유효성 검사 성공")
    void validateRefreshTokenSuccess(){
        //given
        String bearerToken = jwtUtil.createRefreshToken(1L);
        String token = bearerToken.replace(BEARER_PREFIX, " ");

        //when
        boolean validToken = jwtUtil.validateRefreshToken(token);

        //then
        assertTrue(validToken);
    }

    @Nested
    @DisplayName("refreshToken 유효성 검사 실패")
    class validateRefreshTokenFail{
        @Test
        @DisplayName("refreshToken 유효성 검사 실패 - 유효하지 않은 JWT")
        void validateRefreshTokenFail1(){
            String token = "wrong token";
            assertThrows(IllegalArgumentException.class,()->{
                jwtUtil.validateRefreshToken(token);
            });
        }

        @Test
        @DisplayName("refreshToken 유효성 검사 실패 - 만료된 JWT")
        void validateRefreshTokenFail2(){
            String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiLsnbTsmIjsm5AiLCJleHAiOjE2OTQ1OTQ3NzIsImlhdCI6MTY5Mzk4OTk3Mn0.40i7o87JI2K6SHdXg4JU75OtYNPbSvq24G52zE-CEsM";
            assertThrows(IllegalArgumentException.class,()->{
                jwtUtil.validateRefreshToken(token);
            });
        }

        @Test
        @DisplayName("refreshToken 유효성 검사 실패 - 지원되지 않는 JWT")
        void validateRefreshTokenFail3(){
            String token = "eyJhbGciOiJOT05FIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.DjwRE2RJ21X_p-BzUI1OlYh8i4SKLIZryRrRPcXG5Bc";
            assertThrows(IllegalArgumentException.class,()->{
                jwtUtil.validateRefreshToken(token);
            });
        }

        @Test
        @DisplayName("refreshToken 유효성 검사 실패 - 잘못된 JWT")
        void validateRefreshTokenFail4(){
            String token = " ";
            assertThrows(IllegalArgumentException.class,()->{
                jwtUtil.validateRefreshToken(token);
            });
        }
    }

    @Test
    @DisplayName("accessToken에서 사용자 정보 가져오기")
    void getUserInfoFromAccessToken(){
        Long userId = 1L;
        String bearerAccessToken = jwtUtil.createAccessToken(userId,UserRoleEnum.USER);
        String token = bearerAccessToken.substring(7);
        Claims claims = jwtUtil.getUserInfoFromAccessToken(token);
        assertEquals(userId.toString(), claims.getSubject());
    }

    @Test
    @DisplayName("refreshToken에서 사용자 정보 가져오기")
    void getUserInfoFromRefreshToken(){
        Long userId = 1L;
        String bearerRefreshToken = jwtUtil.createRefreshToken(userId);
        String token = bearerRefreshToken.substring(7);
        Long extractedUserId = jwtUtil.getUserInfoFromRefreshToken(token);
        assertEquals(userId, extractedUserId);
    }
}