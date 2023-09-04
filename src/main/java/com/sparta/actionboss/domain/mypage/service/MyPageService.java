package com.sparta.actionboss.domain.mypage.service;

import com.sparta.actionboss.domain.auth.entity.RefreshToken;
import com.sparta.actionboss.domain.auth.entity.User;
import com.sparta.actionboss.domain.auth.repository.RefreshTokenRepository;
import com.sparta.actionboss.domain.auth.repository.UserRepository;
import com.sparta.actionboss.domain.mypage.dto.*;
import com.sparta.actionboss.global.exception.MyPageException;
import com.sparta.actionboss.global.exception.SignupException;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.sparta.actionboss.global.response.SuccessMessage.*;

@Slf4j(topic = "mypage service")
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;


    //마이페이지 유저정보 조회
    public CommonResponse<MyPageInfoResponseDto> getUserInfo(User user) {
        //유저 확인
        User currentUser = userRepository.findByNickname(user.getNickname()).orElseThrow(
                ()-> new MyPageException(ClientErrorCode.NO_ACCOUNT));

        String email = currentUser.getEmail();
        String nickname = currentUser.getNickname();

        if(email == null){
            email = "";
        }

        //유저 정보 보내기
        MyPageInfoResponseDto responseDto = new MyPageInfoResponseDto(email, nickname);
        return new CommonResponse(GET_MYPAGE, responseDto);
    }

    //이메일 등록
    public CommonResponse updateEmail(UpdateEmailRequestDto requestDto, User user) {
        //비어있다면 새로운 이메일 넣어주기
        if(user.getEmail() == null){
            user.updateEmail(requestDto);
            userRepository.save(user);
            return new CommonResponse(UPDATE_EMAIL);
        } else {
            throw new MyPageException(ClientErrorCode.REGISTERED_EMAIL);
        }
    }

    //회원탈퇴
    @Transactional
    public CommonResponse deleteAccount(User user) {
        User currentUser = userRepository.findByNickname(user.getNickname()).orElseThrow(
                ()-> new MyPageException(ClientErrorCode.NO_ACCOUNT));
        userRepository.delete(currentUser);
        return new CommonResponse(DELETE_ACCOUNT);
    }

    //닉네임 수정
    @Transactional
    public CommonResponse<UpdateNicknameResponseDto> updateNickname(UpdateNicknameRequestDto requestDto, User user, HttpServletResponse response) {
        String newNickname = requestDto.getNickname();

        Optional<User> existingUserWithNewNickname = userRepository.findByNickname(newNickname);
        if (existingUserWithNewNickname.isPresent()) {
            throw new MyPageException(ClientErrorCode.DUPLICATE_NICKNAME);
        }

//        if(userRepository.findByNickname(newNickname).isPresent()){
//            throw new MyPageException(ClientErrorCode.DUPLICATE_NICKNAME);
//        }

        String accessToken = jwtUtil.createAccessToken(newNickname, user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(newNickname);

        RefreshToken refreshTokenEntity = new RefreshToken(refreshToken.substring(7), user.getNickname());
        refreshTokenRepository.save(refreshTokenEntity);


        user.updateNickname(newNickname);
        userRepository.save(user);

        UpdateNicknameResponseDto responseDto = new UpdateNicknameResponseDto(accessToken, refreshToken);

        response.addHeader(JwtUtil.AUTHORIZATION_ACCESS, accessToken);
        response.addHeader(JwtUtil.AUTHORIZATION_REFRESH, refreshToken);

        return new CommonResponse(UPDATE_NICKNAME, responseDto);
    }

    //비밀번호 변경
    @Transactional
    public CommonResponse updatePassword(UpdatePasswordRequestDto requestDto, User user) {
        String newPassword = passwordEncoder.encode(requestDto.getPassword());

        userRepository.findByNickname(user.getNickname()).orElseThrow(
                ()-> new MyPageException(ClientErrorCode.NO_ACCOUNT));

        user.updatePassword(newPassword);
        userRepository.save(user);
        return new CommonResponse(UPDATE_PASSWORD);
    }
}
