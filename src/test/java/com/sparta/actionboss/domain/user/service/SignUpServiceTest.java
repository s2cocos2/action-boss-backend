package com.sparta.actionboss.domain.user.service;

import com.sparta.actionboss.domain.user.dto.request.CheckEmailRequestDto;
import com.sparta.actionboss.domain.user.dto.request.CheckNicknameRequestDto;
import com.sparta.actionboss.domain.user.dto.request.SendEmailRequestDto;
import com.sparta.actionboss.domain.user.dto.request.SignupRequestDto;
import com.sparta.actionboss.domain.user.entity.Email;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.repository.EmailRepository;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.util.EmailUtil;
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
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    EmailRepository emailRepository;

    @Mock
    EmailUtil emailUtil;

    SignUpService signUpService;

    @BeforeEach
    void setUp(){
        signUpService = new SignUpService(userRepository, passwordEncoder, emailRepository, emailUtil);
    }

    @Nested
    @DisplayName("회원가입")
    class signUpTest{
        @Test
        @DisplayName("회원가입 성공 - 일반회원")
        void signUpSuccess(){
            //given
            SignupRequestDto requestDto = new SignupRequestDto("coco@naver.com", "abcd1234", "코코", "123456");
            String email = requestDto.getEmail();
            String password = passwordEncoder.encode(requestDto.getPassword());
            String nickname = requestDto.getNickname();
            String successKey = requestDto.getSuccessKey();
            Long emailId = 1L;
            User user = new User(nickname, password, email, UserRoleEnum.USER);

            given(passwordEncoder.encode(requestDto.getPassword())).willReturn("encodedPassword");
            given(emailRepository.findByEmail(email)).willReturn(Optional.of(new Email(emailId, email, successKey)));
            given(userRepository.save(any(User.class))).willReturn(user);

            //when
            CommonResponse result = signUpService.signup(requestDto);

            //then
            assertEquals("회원가입에 성공하였습니다.", result.getMsg());
        }

        @Test
        @DisplayName("회원가입 성공 - admin")
        void signUpSuccess2(){
            //given
            SignupRequestDto requestDto = new SignupRequestDto("coco@naver.com", "abcd1234", "코코", "123456",true,"AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC");
            String email = requestDto.getEmail();
            String password = passwordEncoder.encode(requestDto.getPassword());
            String nickname = requestDto.getNickname();
            String successKey = requestDto.getSuccessKey();
            Long emailId = 1L;
            User user = new User(nickname, password, email, UserRoleEnum.USER);

            given(passwordEncoder.encode(requestDto.getPassword())).willReturn("encodedPassword");
            given(emailRepository.findByEmail(email)).willReturn(Optional.of(new Email(emailId, email, successKey)));
            given(userRepository.save(any(User.class))).willReturn(user);

            //when
            CommonResponse result = signUpService.signup(requestDto);

            //then
            assertEquals("회원가입에 성공하였습니다.", result.getMsg());
        }

        @Test
        @DisplayName("회원가입 실패 - 닉네임 중복")
        void signUpFail(){
            //given
            SignupRequestDto requestDto = new SignupRequestDto("coco@naver.com", "abcd1234", "코코", "123456");
            String nickname = requestDto.getNickname();

            User existingUser = new User("코코","abcd1234","coco2@naver.com",UserRoleEnum.USER);

            given(userRepository.findByNickname(nickname)).willReturn(Optional.of(existingUser));

            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                signUpService.signup(requestDto);
            });

            //then
            assertEquals("이미 존재하는 닉네임입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("회원가입 실패 - admin 가입 시 토큰이 유효하지 않을 때")
        void signUpFail2(){
            //given
            SignupRequestDto requestDto = new SignupRequestDto("coco@naver.com", "abcd1234", "코코", "123456",true,"abcd");

            given(emailRepository.findByEmail(requestDto.getEmail())).willReturn(Optional.of(new Email(1L, requestDto.getEmail(), requestDto.getSuccessKey())));

            //when
            Exception exception = assertThrows(CommonException.class,()->{
                signUpService.signup(requestDto);
            });

            //then
            assertEquals("관리자 암호가 일치하지 않습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("회원가입 실패 - 회원가입이 정상적으로 이루어지지 않을 때")
        void signUpFail3(){
            //given
            SignupRequestDto requestDto = new SignupRequestDto("coco@naver.com", "abcd1234", "코코", "123456");

            given(emailRepository.findByEmail(requestDto.getEmail())).willReturn(Optional.of(new Email(1L, requestDto.getEmail(), requestDto.getSuccessKey())));
            given(userRepository.save(any(User.class))).willReturn(null);

            //when
            Exception exception = assertThrows(CommonException.class,()->{
                signUpService.signup(requestDto);
            });

            //then
            assertEquals("회원가입에 실패하였습니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("닉네임 중복 확인")
    class checkNickname{

        @Test
        @DisplayName("닉네임 중복 확인 - 성공")
        void checkNicknameSuccess(){
            //given
            CheckNicknameRequestDto requestDto = new CheckNicknameRequestDto("코코");

            given(userRepository.findByNickname(requestDto.nickname())).willReturn(Optional.empty());

            //when
            CommonResponse result = signUpService.checkNickname(requestDto);

            //then
            assertEquals("사용 가능한 닉네임입니다.", result.getMsg());
        }

        @Test
        @DisplayName("닉네임 중복 확인 - 실패")
        void checkNicknameFail(){
            //given
            CheckNicknameRequestDto requestDto = new CheckNicknameRequestDto("코코");

            given(userRepository.findByNickname(requestDto.nickname())).willReturn(Optional.of(new User()));

            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                signUpService.checkNickname(requestDto);
            });

            //then
            assertEquals("이미 존재하는 닉네임입니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("이메일 전송")
    class sendEmail{

        @Test
        @DisplayName("이메일 전송 성공 - 이메일이 DB에 없을 때")
        void sendEmailSuccess1(){
            //given
            SendEmailRequestDto requestDto = new SendEmailRequestDto("coco@naver.com");

            given(userRepository.findByEmail(requestDto.email())).willReturn(Optional.empty());
            given(emailRepository.findByEmail(requestDto.email())).willReturn(Optional.empty());

            Email email = Email.builder()
                    .email(requestDto.email())
                    .successKey("123456")
                    .build();
            given(emailRepository.save(any(Email.class))).willReturn(email);

            //when
            CommonResponse result = signUpService.sendEmail(requestDto);

            //then
            assertEquals("이메일 인증 코드을 보냈습니다.", result.getMsg());
        }

        @Test
        @DisplayName("이메일 전송 성공 - 이메일이 DB에 있을 때 ")
        void sendEmailSuccess2(){
            //given
            SendEmailRequestDto requestDto = new SendEmailRequestDto("coco@naver.com");

            given(userRepository.findByEmail(requestDto.email())).willReturn(Optional.empty());
            given(emailRepository.findByEmail(requestDto.email())).willReturn(Optional.of(new Email()));

            //when
            CommonResponse result = signUpService.sendEmail(requestDto);

            //then
            assertEquals("이메일 인증 코드을 보냈습니다.", result.getMsg());
        }

        @Test
        @DisplayName("이메일 전송 실패 - 중복된 이메일일 때")
        void sendEmailFail1(){
            //given
            SendEmailRequestDto requestDto = new SendEmailRequestDto("coco@naver.com");

            given(userRepository.findByEmail(requestDto.email())).willReturn(Optional.of(new User()));

            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                signUpService.sendEmail(requestDto);
            });

            //then
            assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("이메일 전송 실패")
        void sendEmailFail2(){
            //given
            SendEmailRequestDto requestDto = new SendEmailRequestDto("coco@naver.com");

            given(userRepository.findByEmail(requestDto.email())).willReturn(Optional.empty());
            given(emailRepository.findByEmail(requestDto.email())).willReturn(Optional.of(new Email()));

            willThrow(new CommonException(ClientErrorCode.EMAIL_SENDING_FAILED)).given(emailUtil).sendEmail(anyString(),anyString());

            //when
            Exception exception = assertThrows(CommonException.class, ()->{
                signUpService.sendEmail(requestDto);
            });

            //then
            assertEquals("이메일 인증 코드를 보내지 못했습니다.", exception.getMessage());
        }
    }

    @Test
    @DisplayName("이메일 확인 성공")
    void checkEmail(){
        //given
        CheckEmailRequestDto requestDto = new CheckEmailRequestDto("coco@naver.com", "123456");
        Email email = Email.builder().id(1L).email(requestDto.email()).successKey(requestDto.successKey()).build();

        given(emailRepository.findByEmail(requestDto.email())).willReturn(Optional.of(email));

        //when
        CommonResponse result = signUpService.checkEmail(requestDto);

        //then
        assertEquals("이메일 인증이 완료되었습니다.", result.getMsg());
    }

    @Nested
    @DisplayName("이메일 인증번호 확인")
    class checkEmailSuccessKey{

        @Test
        @DisplayName("이메일 인증번호 확인 실패 - 이메일이 DB에 저장되어 있지 않을 때")
        void checkEmailSuccessKeyFail1(){
            //given
            String requestEmail = "coco@naver.com";
            String successKey = "123456";

            given(emailRepository.findByEmail(requestEmail)).willReturn(Optional.empty());

            //when
            Exception exception = assertThrows(CommonException.class, ()-> {
                signUpService.checkEmailSuccessKey(requestEmail, successKey);
            });

            //then
            assertEquals("가입되지 않은 계정입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("이메일 인증번호 확인 실패 - 입력한 번호가 맞지 않을 때")
        void checkEmailSuccessKeyFail2(){
            //given
            String requestEmail = "coco@naver.com";
            String successKey = "123456";
            Email email = Email.builder()
                    .id(1L)
                    .email(requestEmail)
                    .successKey("654321")
                    .build();

            given(emailRepository.findByEmail(requestEmail)).willReturn(Optional.of(email));

            //when
            Exception exception = assertThrows(CommonException.class, ()-> {
                signUpService.checkEmailSuccessKey(requestEmail, successKey);
            });

            //then
            assertEquals("이메일 인증에 실패하였습니다.", exception.getMessage());
        }
    }
}