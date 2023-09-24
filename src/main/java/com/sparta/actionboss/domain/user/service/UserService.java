package com.sparta.actionboss.domain.user.service;

import com.sparta.actionboss.domain.user.dto.request.*;
import com.sparta.actionboss.domain.user.entity.Email;
import com.sparta.actionboss.domain.user.entity.RefreshToken;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import com.sparta.actionboss.domain.user.repository.EmailRepository;
import com.sparta.actionboss.domain.user.repository.RefreshTokenRepository;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.util.EmailUtil;
import com.sparta.actionboss.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static com.sparta.actionboss.global.response.SuccessMessage.*;
import static com.sparta.actionboss.global.util.JwtUtil.AUTHORIZATION_REFRESH;

@Slf4j(topic = "UserService")
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailRepository emailRepository;
    private final EmailUtil emailUtil;
    private final JwtUtil jwtUtil;


    // ADMIN_TOKEN
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    //회원가입
    @Transactional
    public CommonResponse signup(SignupRequestDto requestDto) {
        String nickname = requestDto.getNickname();
        String email = requestDto.getEmail();
        String password = passwordEncoder.encode(requestDto.getPassword());

        //닉네임 중복확인
        if(checkNickname(nickname)){
            throw new CommonException(ClientErrorCode.DUPLICATE_NICKNAME);
        }

        long emailId = checkEmailSuccessKey(requestDto.getEmail(), requestDto.getSuccessKey());
        emailRepository.deleteById(emailId);

        // 사용자 ROLE 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.isAdmin()) {
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new CommonException(ClientErrorCode.INVALID_ADMIN_TOKEN);
            }
            role = UserRoleEnum.ADMIN;
        }

        User user = new User(nickname, password, email, role);
        User savedUser =  userRepository.save(user);
        if(savedUser == null){
            throw new CommonException(ClientErrorCode.SIGNUP_FAILED);
        }
        return new CommonResponse(SIGNUP_SUCCESS);
    }

    //로그인
    @Transactional
    public CommonResponse login(LoginRequestDto requestDto, HttpServletResponse response){

        User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(() ->
                new CommonException(ClientErrorCode.NO_ACCOUNT));
        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())){
            throw new CommonException(ClientErrorCode.INVALID_PASSWORDS);
        }
        String accessToken = jwtUtil.createAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getUserId());

        refreshTokenRepository.deleteByUserId(user.getUserId());

        RefreshToken refreshTokenEntity = new RefreshToken(refreshToken.substring(7), user.getUserId());
        refreshTokenRepository.save(refreshTokenEntity);

        response.addHeader(JwtUtil.AUTHORIZATION_ACCESS, accessToken);
        response.addHeader(JwtUtil.AUTHORIZATION_REFRESH, refreshToken);

        return new CommonResponse(LOGIN_SUCCESS);
    }

    //토큰 재발행
    public CommonResponse reissueToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = jwtUtil.getJwtFromHeader(request, AUTHORIZATION_REFRESH);

        if (StringUtils.hasText(refreshToken)) {
            if (jwtUtil.validateRefreshToken(refreshToken)) {

                refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                        ()-> new CommonException(ClientErrorCode.NO_REFRESHTOKEN));

                Long userId = jwtUtil.getUserInfoFromRefreshToken(refreshToken);

                String newAccessToken = jwtUtil.createAccessToken(userId, UserRoleEnum.USER);

                response.addHeader(JwtUtil.AUTHORIZATION_ACCESS, newAccessToken);
                return new CommonResponse(CREATE_REFRESHTOKEN);
            }
        }
        throw new CommonException(ClientErrorCode.INVALID_REFRESHTOKEN);
    }

    @Transactional(readOnly = true)
    public CommonResponse checkNickname(CheckNicknameRequestDto requestDto) {
        String nickname = requestDto.getNickname();
        if (checkNickname(nickname)) {
            throw new CommonException(ClientErrorCode.DUPLICATE_NICKNAME);
        }
        return new CommonResponse(AVAILABLE_NICKNAME);
    }

    @Transactional
    public CommonResponse sendEmail(SendEmailRequestDto requestDto) {

        userRepository.findByEmail(requestDto.getEmail()).ifPresent(existingUser -> {
            throw new CommonException(ClientErrorCode.DUPLICATE_EMAIL);
        });

        Optional<Email> email = emailRepository.findByEmail(requestDto.getEmail());

        CommonResponse response = new CommonResponse(SEND_EMAIL_CODE);

        String successKey = emailUtil.makeRandomNumber();

        try{
            emailUtil.sendEmail(requestDto.getEmail(), successKey);
        } catch (CommonException e) {
            throw new CommonException(ClientErrorCode.EMAIL_SENDING_FAILED);
        }
        if (email.isEmpty()) {
            emailRepository.save(Email.builder()
                    .email(requestDto.getEmail())
                    .successKey(successKey)
                    .build());
            return response;
        }
        email.get().changeSuccessKey(successKey);
        return response;
    }

    @Transactional
    public CommonResponse checkEmail(CheckEmailRequestDto requestDto) {
        checkEmailSuccessKey(requestDto.getEmail(), requestDto.getSuccessKey());
        return new CommonResponse(EMAIL_AUTHENTICATE_SUCCESS);
    }

    private long checkEmailSuccessKey(String requestEmail, String successKey) {
        Email email = emailRepository.findByEmail(requestEmail).orElseThrow(
                ()-> new CommonException(ClientErrorCode.NO_ACCOUNT));
        if(!email.getSuccessKey().equals(successKey)){
            throw new CommonException(ClientErrorCode.EMAIL_AUTHENTICATION_FAILED);
        }
        return email.getId();
    }

    private boolean checkNickname(String nickname){
        Optional<User> checkNickname = userRepository.findByNickname(nickname);
        if(checkNickname.isPresent()){
            return true;
        } else {
            return false;
        }
    }
}
