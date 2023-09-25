package com.sparta.actionboss.domain.mypage.service;

import com.sparta.actionboss.domain.mypage.dto.request.UpdateEmailRequestDto;
import com.sparta.actionboss.domain.mypage.dto.request.UpdateNicknameRequestDto;
import com.sparta.actionboss.domain.mypage.dto.request.UpdatePasswordRequestDto;
import com.sparta.actionboss.domain.mypage.dto.response.MyPageInfoResponseDto;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sparta.actionboss.global.exception.errorcode.ClientErrorCode.*;
import static com.sparta.actionboss.global.response.SuccessMessage.*;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public CommonResponse<MyPageInfoResponseDto> getUserInfo(User user) {
        User currentUser = userRepository.findByNickname(user.getNickname()).orElseThrow(
                ()-> new CommonException(NO_ACCOUNT));
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
        String nickname = currentUser.getNickname();
        MyPageInfoResponseDto responseDto = new MyPageInfoResponseDto(email, nickname);
        return new CommonResponse(GET_MYPAGE, responseDto);
    }


    public CommonResponse updateEmail(UpdateEmailRequestDto requestDto, User user) {
        if(user.getEmail() == null){
            user.updateEmail(requestDto);
            userRepository.save(user);
            return new CommonResponse(UPDATE_EMAIL);
        } else {
            throw new CommonException(REGISTERED_EMAIL);
        }
    }

    public CommonResponse deleteAccount(User user) {
        User currentUser = userRepository.findByNickname(user.getNickname()).orElseThrow(
                ()-> new CommonException(NO_ACCOUNT));
        userRepository.delete(currentUser);
        return new CommonResponse(DELETE_ACCOUNT);
    }


    public CommonResponse updateNickname(UpdateNicknameRequestDto requestDto, User user) {
        String newNickname = requestDto.nickname();
        if(userRepository.findByNickname(newNickname).isPresent()){
            throw new CommonException(DUPLICATE_NICKNAME);
        }
        user.updateNickname(newNickname);
        userRepository.save(user);
        return new CommonResponse(UPDATE_NICKNAME);
    }


    public CommonResponse updatePassword(UpdatePasswordRequestDto requestDto, User user) {
        String newPassword = passwordEncoder.encode(requestDto.password());
        userRepository.findByNickname(user.getNickname()).orElseThrow(
                ()-> new CommonException(NO_ACCOUNT));
        user.updatePassword(newPassword);
        userRepository.save(user);
        return new CommonResponse(UPDATE_PASSWORD);
    }
}
