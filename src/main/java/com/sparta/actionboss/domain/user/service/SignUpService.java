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
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.sparta.actionboss.global.exception.errorcode.ClientErrorCode.*;
import static com.sparta.actionboss.global.response.SuccessMessage.*;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailRepository emailRepository;
    private final EmailUtil emailUtil;


    // ADMIN_TOKEN
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    public CommonResponse signup(SignupRequestDto requestDto) {
        String nickname = requestDto.getNickname();
        String email = requestDto.getEmail();
        String password = passwordEncoder.encode(requestDto.getPassword());

        if(existingNickname(nickname)){
            throw new CommonException(DUPLICATE_NICKNAME);
        }

        long emailId = checkEmailSuccessKey(requestDto.getEmail(), requestDto.getSuccessKey());
        emailRepository.deleteById(emailId);

        // 사용자 ROLE 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.isAdmin()) {
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new CommonException(INVALID_ADMIN_TOKEN);
            }
            role = UserRoleEnum.ADMIN;
        }

        User user = new User(nickname, password, email, role);
        User savedUser =  userRepository.save(user);
        if(savedUser == null){
            throw new CommonException(SIGNUP_FAILED);
        }
        return new CommonResponse(SIGNUP_SUCCESS);
    }


    public CommonResponse checkNickname(CheckNicknameRequestDto requestDto) {
        String nickname = requestDto.nickname();
        if (existingNickname(nickname)) {
            throw new CommonException(DUPLICATE_NICKNAME);
        }
        return new CommonResponse(AVAILABLE_NICKNAME);
    }


    public boolean existingNickname(String nickname){
        Optional<User> existingNickname = userRepository.findByNickname(nickname);
        if(existingNickname.isPresent()){
            return true;
        } else {
            return false;
        }
    }


    public CommonResponse sendEmail(SendEmailRequestDto requestDto) {

        userRepository.findByEmail(requestDto.email()).ifPresent(existingUser -> {
            throw new CommonException(DUPLICATE_EMAIL);
        });

        Optional<Email> email = emailRepository.findByEmail(requestDto.email());
        CommonResponse response = new CommonResponse(SEND_EMAIL_CODE);
        String successKey = emailUtil.makeRandomNumber();

        try{
            emailUtil.sendEmail(requestDto.email(), successKey);
        } catch (CommonException e) {
            throw new CommonException(EMAIL_SENDING_FAILED);
        }
        if (email.isEmpty()) {
            emailRepository.save(Email.builder()
                    .email(requestDto.email())
                    .successKey(successKey)
                    .build());
            return response;
        }
        email.get().changeSuccessKey(successKey);
        return response;
    }


    public CommonResponse checkEmail(CheckEmailRequestDto requestDto) {
        checkEmailSuccessKey(requestDto.email(), requestDto.successKey());
        return new CommonResponse(EMAIL_AUTHENTICATE_SUCCESS);
    }

    public long checkEmailSuccessKey(String requestEmail, String successKey) {
        Email email = emailRepository.findByEmail(requestEmail).orElseThrow(
                ()-> new CommonException(NO_ACCOUNT));
        if(!email.getSuccessKey().equals(successKey)){
            throw new CommonException(EMAIL_AUTHENTICATION_FAILED);
        }
        return email.getId();
    }
}
