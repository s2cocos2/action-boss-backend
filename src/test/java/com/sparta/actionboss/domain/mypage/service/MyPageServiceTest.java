package com.sparta.actionboss.domain.mypage.service;

import com.sparta.actionboss.domain.mypage.dto.request.UpdateEmailRequestDto;
import com.sparta.actionboss.domain.mypage.dto.request.UpdateNicknameRequestDto;
import com.sparta.actionboss.domain.mypage.dto.request.UpdatePasswordRequestDto;
import com.sparta.actionboss.domain.mypage.dto.response.MyPageInfoResponseDto;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.response.CommonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    MyPageService myPageService;

    @BeforeEach
    void setUp(){
        myPageService = new MyPageService(userRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("마이페이지 조회")
    class getUserInfoTest{
        @Test
        @DisplayName("마이페이지 조회 - 성공")
        void userInfoSuccess(){
            //given
            User user = new User("코코","abcd1234","coco@naver.com",UserRoleEnum.USER);

            given(userRepository.findByNickname(user.getNickname())).willReturn(Optional.of(user));

            //when
            CommonResponse<MyPageInfoResponseDto> result = myPageService.getUserInfo(user);

            //then
            assertEquals("마이페이지 조회에 성공하였습니다.", result.getMsg());
        }
        @Test
        @DisplayName("마이페이지 조회 - 실패")
        void userInfoFail(){
            //given
            User user = new User("코코","abcd1234","coco@naver.com",UserRoleEnum.USER);

            given(userRepository.findByNickname(user.getNickname())).willReturn(Optional.empty());

            //when
            Exception exception = assertThrows(CommonException.class, () -> {
                myPageService.getUserInfo(user);
            });

            //then
            assertEquals("가입되지 않은 계정입니다.", exception.getMessage());
        }
    }


    @Nested
    @DisplayName("이메일 업데이트")
    class updateEmail {
        @Test
        @DisplayName("이메일 업데이트 - 성공")
        void updateEmailSuccess(){
            //given
            UpdateEmailRequestDto requestDto = new UpdateEmailRequestDto("coco2@naver.com");
            User user = new User("코코","abcd1234",UserRoleEnum.USER,1234L);

            //when
            CommonResponse result = myPageService.updateEmail(requestDto, user);

            //then
            assertEquals("이메일 등록에 성공하였습니다.", result.getMsg());
        }

        @Test
        @DisplayName("이메일 업데이트 - 실패")
        void updateEmailFail(){
            //given
            UpdateEmailRequestDto requestDto = new UpdateEmailRequestDto("coco@naver.com");
            User user = new User("코코","abcd1234","coco@naver.com",UserRoleEnum.USER);

            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                myPageService.updateEmail(requestDto, user);
            });

            //then
            assertEquals("등록된 이메일이 있습니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("회원탈퇴")
    class deleteAccount{

        @Test
        @DisplayName("회원탈퇴 - 성공")
        void deleteAccountSuccess(){
            //given
            User user = new User("코코","abcd1234","coco@naver.com",UserRoleEnum.USER);

            given(userRepository.findByNickname(user.getNickname())).willReturn(Optional.of(user));

            //when
            CommonResponse result = myPageService.deleteAccount(user);

            //then
            assertEquals("회원 탈퇴에 성공하였습니다.", result.getMsg());
        }

        @Test
        @DisplayName("회원탈퇴 - 실패")
        void deleteAccountFail(){
            //given
            User user = new User("코코","abcd1234","coco@naver.com",UserRoleEnum.USER);

            given(userRepository.findByNickname(user.getNickname())).willReturn(Optional.empty());

            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                myPageService.deleteAccount(user);
            });

            //then
            assertEquals("가입되지 않은 계정입니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("닉네임 업데이트")
    class updateNickname{

        @Test
        @DisplayName("닉네임 업데이트 - 성공")
        void updateNicknameSuccess(){
            //given
            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("코코입니다");
            User user = new User("코코","abcd1234","coco@naver.com",UserRoleEnum.USER);

            //when
            CommonResponse result = myPageService.updateNickname(requestDto, user);

            //then
            assertEquals("닉네임 수정에 성공하였습니다.", result.getMsg());
        }

        @Test
        @DisplayName("닉네임 업데이트 - 실패")
        void updateNicknameFail(){
            //given
            UpdateNicknameRequestDto requestDto = new UpdateNicknameRequestDto("코코");
            User user = new User("코코","abcd1234","coco@naver.com",UserRoleEnum.USER);

            given(userRepository.findByNickname(user.getNickname())).willReturn(Optional.of(user));

            //when
            Exception exception = assertThrows(CommonException.class,()->{
                myPageService.updateNickname(requestDto, user);
            });

            //then
            assertEquals("이미 존재하는 닉네임입니다.", exception.getMessage());
        }

        @Nested
        @DisplayName("비밀번호 업데이트")
        class updatePassword{

            @Test
            @DisplayName("비밀번호 업데이트 - 성공")
            void updatePasswordSuccess(){
                //given
                UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("abcd1212");
                User user = new User("코코","abcd1234","coco@naver.com",UserRoleEnum.USER);

                given(userRepository.findByNickname(user.getNickname())).willReturn(Optional.of(user));
                //when
                CommonResponse result = myPageService.updatePassword(requestDto, user);

                //then
                assertEquals("비밀번호 수정에 성공하였습니다.", result.getMsg());
            }

            @Test
            @DisplayName("비밀번호 업데이트 - 실패")
            void updatePasswordFail(){
                //given
                UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("abcd1212");
                User user = new User("코코","abcd1234","coco@naver.com",UserRoleEnum.USER);

                given(userRepository.findByNickname(user.getNickname())).willReturn(Optional.empty());

                //when
                Exception exception = assertThrows(CommonException.class,()->{
                    myPageService.updatePassword(requestDto, user);
                });

                //then
                assertEquals("가입되지 않은 계정입니다.", exception.getMessage());
            }
        }
    }
}