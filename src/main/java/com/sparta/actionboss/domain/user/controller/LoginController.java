package com.sparta.actionboss.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.actionboss.domain.user.dto.request.LoginRequestDto;
import com.sparta.actionboss.domain.user.service.KakaoService;
import com.sparta.actionboss.domain.user.service.LoginService;
import com.sparta.actionboss.domain.user.service.SignUpService;
import com.sparta.actionboss.global.response.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;
    private final KakaoService kakaoService;

    @PostMapping("/login")
    public ResponseEntity<CommonResponse> login(@RequestBody @Valid LoginRequestDto requestDto, HttpServletResponse response){
        return new ResponseEntity<>(loginService.login(requestDto, response), HttpStatus.OK);
    }

    @GetMapping("/login/reissueToken")
    public ResponseEntity<CommonResponse> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        return new ResponseEntity<>(loginService.reissueToken(request, response), HttpStatus.CREATED);
    }

    @PostMapping("/kakao")
    public ResponseEntity<CommonResponse> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        return new ResponseEntity<>(kakaoService.kakaoLogin(code, response), HttpStatus.OK);
    }
}
