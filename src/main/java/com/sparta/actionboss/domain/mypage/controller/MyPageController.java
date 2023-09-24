package com.sparta.actionboss.domain.mypage.controller;

import com.sparta.actionboss.domain.mypage.dto.request.UpdateEmailRequestDto;
import com.sparta.actionboss.domain.mypage.dto.request.UpdateNicknameRequestDto;
import com.sparta.actionboss.domain.mypage.dto.request.UpdatePasswordRequestDto;
import com.sparta.actionboss.domain.mypage.dto.response.MyPageInfoResponseDto;
import com.sparta.actionboss.domain.mypage.service.MyPageService;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/getUserInfo")
    public ResponseEntity<CommonResponse<MyPageInfoResponseDto>> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return new ResponseEntity<>(myPageService.getUserInfo(userDetails.getUser()), HttpStatus.OK);
    }

    @PatchMapping("/updateEmail")
    public ResponseEntity<CommonResponse> updateEmail(@RequestBody @Valid UpdateEmailRequestDto requestDto,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails){
       return new ResponseEntity<>(myPageService.updateEmail(requestDto, userDetails.getUser()), HttpStatus.CREATED);
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<CommonResponse> deleteAccount(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return new ResponseEntity<>(myPageService.deleteAccount(userDetails.getUser()), HttpStatus.OK);
    }

    @PatchMapping("/updateNickname")
    public ResponseEntity<CommonResponse> updateNickname(@RequestBody @Valid UpdateNicknameRequestDto requestDto,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails){
        return new ResponseEntity<>(myPageService.updateNickname(requestDto, userDetails.getUser()), HttpStatus.CREATED);
    }

    @PatchMapping("/updatePassword")
    public ResponseEntity<CommonResponse> updatePassword(@RequestBody @Valid UpdatePasswordRequestDto requestDto,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails){
        return new ResponseEntity<>(myPageService.updatePassword(requestDto, userDetails.getUser()), HttpStatus.CREATED);
    }
}
