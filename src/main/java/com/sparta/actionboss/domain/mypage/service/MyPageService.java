package com.sparta.actionboss.domain.mypage.service;

import com.sparta.actionboss.domain.user.entity.RefreshToken;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.repository.RefreshTokenRepository;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.domain.mypage.dto.response.MyPageInfoResponseDto;
import com.sparta.actionboss.domain.mypage.dto.request.UpdateEmailRequestDto;
import com.sparta.actionboss.domain.mypage.dto.request.UpdateNicknameRequestDto;
import com.sparta.actionboss.domain.mypage.dto.request.UpdatePasswordRequestDto;
import com.sparta.actionboss.global.exception.MyPageException;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sparta.actionboss.global.response.SuccessMessage.*;

@Slf4j(topic = "mypage service")
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public CommonResponse<MyPageInfoResponseDto> getUserInfo(User user) {
        User currentUser = userRepository.findByNickname(user.getNickname()).orElseThrow(
                ()-> new MyPageException(ClientErrorCode.NO_ACCOUNT));

        String email = currentUser.getEmail();
        String nickname = currentUser.getNickname();

        if(email == null){
            email = "";
        }
        MyPageInfoResponseDto responseDto = new MyPageInfoResponseDto(email, nickname);
        return new CommonResponse(GET_MYPAGE, responseDto);
    }

    public CommonResponse updateEmail(UpdateEmailRequestDto requestDto, User user) {
        if(user.getEmail() == null){
            user.updateEmail(requestDto);
            userRepository.save(user);
            return new CommonResponse(UPDATE_EMAIL);
        } else {
            throw new MyPageException(ClientErrorCode.REGISTERED_EMAIL);
        }
    }

    @Transactional
    public CommonResponse deleteAccount(User user) {
        User currentUser = userRepository.findByNickname(user.getNickname()).orElseThrow(
                ()-> new MyPageException(ClientErrorCode.NO_ACCOUNT));
        userRepository.delete(currentUser);
        return new CommonResponse(DELETE_ACCOUNT);
    }

    @Transactional
    public CommonResponse updateNickname(UpdateNicknameRequestDto requestDto, User user, HttpServletResponse response) {
        String newNickname = requestDto.getNickname();

        if(userRepository.findByNickname(newNickname).isPresent()){
            throw new MyPageException(ClientErrorCode.DUPLICATE_NICKNAME);
        }

        refreshTokenRepository.deleteByUserId(user.getUserId());

        String accessToken = jwtUtil.createAccessToken(newNickname, user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(newNickname);

        RefreshToken refreshTokenEntity = new RefreshToken(refreshToken.substring(7), user.getUserId());
        refreshTokenRepository.save(refreshTokenEntity);

        user.updateNickname(newNickname);
        userRepository.save(user);

        response.addHeader(JwtUtil.AUTHORIZATION_ACCESS, accessToken);
        response.addHeader(JwtUtil.AUTHORIZATION_REFRESH, refreshToken);

        return new CommonResponse(UPDATE_NICKNAME);
    }

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
