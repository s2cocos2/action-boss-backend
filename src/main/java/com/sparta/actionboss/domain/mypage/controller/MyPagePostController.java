package com.sparta.actionboss.domain.mypage.controller;

import com.sparta.actionboss.domain.mypage.dto.response.PagingResponseDto;
import com.sparta.actionboss.domain.mypage.service.MyPagePostService;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPagePostController {
    private final MyPagePostService myPagePostService;

    @GetMapping("/myposts")
    public ResponseEntity<CommonResponse<PagingResponseDto>> getMyPosts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(page = 0, size = 7, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(myPagePostService.getMyPosts(
                userDetails.getUser(), pageable),
                HttpStatus.OK)
                ;
    }

    @GetMapping("/agrees")
    public ResponseEntity<CommonResponse> getMyAgrees(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(page = 0, size = 7, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(myPagePostService.getMyAgrees(
                userDetails.getUser(), pageable),
                HttpStatus.OK
        );
    }

    @GetMapping("/comments")
    public ResponseEntity<CommonResponse> getMyComments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(page = 0, size = 7, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(myPagePostService.getMyComments(
                userDetails.getUser(), pageable),
                HttpStatus.OK)
                ;
    }
}
