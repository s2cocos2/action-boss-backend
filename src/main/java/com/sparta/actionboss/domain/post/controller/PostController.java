package com.sparta.actionboss.domain.post.controller;


import com.sparta.actionboss.domain.post.dto.request.PostRequestDto;
import com.sparta.actionboss.domain.post.dto.response.PostResponseDto;
import com.sparta.actionboss.domain.post.service.PostService;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 민원글 작성
    @PostMapping("")
    public ResponseEntity<CommonResponse> createPost(
            @RequestPart(name = "post") PostRequestDto postRequestDto,
            @RequestPart(value = "images") List<MultipartFile> images,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws IOException {
        return new ResponseEntity<>(postService.createPost(
                postRequestDto,
                images, userDetails.getUser()),
                HttpStatus.CREATED);
    }

    // 민원글 상세 조회 (postId)
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponse<PostResponseDto>> getPost(@PathVariable Long postId) {
        return new ResponseEntity<>(postService.getPost(postId), HttpStatus.OK);
    }


    // 민원글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<CommonResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody PostRequestDto postRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return new ResponseEntity<>(postService.updatePost(postId, postRequestDto, userDetails.getUser()), HttpStatus.CREATED);
    }


    // 민원글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<CommonResponse> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return new ResponseEntity<>(postService.deletePost(
                postId,
                userDetails.getUser()), HttpStatus.OK);
    }

    @PostMapping("/{postId}/agree")
    public ResponseEntity<CommonResponse> agreePost(@PathVariable Long postId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return new ResponseEntity<>(postService.agreePost(postId, userDetails.getUser()), HttpStatus.OK);
    }

    @PostMapping("/{postId}/done")
    public ResponseEntity<CommonResponse> createLike(@PathVariable Long postId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return new ResponseEntity<>(postService.createDone(postId, userDetails.getUser()), HttpStatus.OK);
    }
}
