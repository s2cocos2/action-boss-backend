package com.sparta.actionboss.domain.mypage.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.actionboss.domain.post.entity.Agree;
import com.sparta.actionboss.domain.post.entity.Comment;
import com.sparta.actionboss.domain.post.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyPagePostsResponseDto {
    private final Long postId;
    private final String title;
    private final Boolean done;
    private final int agreeCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy.MM.dd", timezone = "Asia/Seoul")
    private final LocalDateTime createdDay;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
    private final LocalDateTime createdTime;

    public MyPagePostsResponseDto(Post post){
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.done = post.isDone();
        this.agreeCount = post.getAgreeCount();
        this.createdDay = post.getCreatedAt();
        this.createdTime = post.getCreatedAt();
    }

    public MyPagePostsResponseDto(Post post, Comment comment){
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.done = post.isDone();
        this.agreeCount = post.getAgreeCount();
        this.createdDay = comment.getCreatedAt();
        this.createdTime = comment.getCreatedAt();
    }

    public MyPagePostsResponseDto(Post post, Agree agree){
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.done = post.isDone();
        this.agreeCount = post.getAgreeCount();
        this.createdDay = agree.getCreatedAt();
        this.createdTime = agree.getCreatedAt();
    }
}
