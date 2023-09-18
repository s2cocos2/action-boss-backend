package com.sparta.actionboss.domain.post.controller;

import com.sparta.actionboss.domain.post.dto.request.CommentRequestDto;
import com.sparta.actionboss.domain.post.service.CommentService;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;


    // 민원글 작성
    @PostMapping("/{postId}")
    public ResponseEntity<CommonResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDto commentRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws IOException {
        return new ResponseEntity<>(commentService.createComment(
                postId,
                commentRequestDto,
                userDetails.getUser()),
                HttpStatus.CREATED);
    }


    // 민원글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommonResponse> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return new ResponseEntity<>(commentService.deleteComment(
                commentId,
                userDetails.getUser()), HttpStatus.OK);
    }
}
