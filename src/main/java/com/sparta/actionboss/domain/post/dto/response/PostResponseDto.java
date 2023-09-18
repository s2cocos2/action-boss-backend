package com.sparta.actionboss.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.post.entity.Comment;
import com.sparta.actionboss.domain.post.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class PostResponseDto {
    private final Long postId;
    private String title;
    private String content;
    private List<String> imageUrlList;
    private String address;
    private Double latitude;
    private Double longitude;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDateTime createdAt;
    private String nickname;
    private Integer doneCount;
    private Boolean done;
    private Boolean owner;
    private Boolean agree;
    private Integer agreeCount;
    private Boolean postDone;
    private List<CommentResponseDto> comments;

    public PostResponseDto(Post post, List<String> imageURLs, boolean done, boolean owner, boolean agree, List<Comment> comments, User loginUser) {
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.imageUrlList = imageURLs;
        this.address = post.getAddress();
        this.latitude = post.getLatitude();
        this.longitude = post.getLongitude();
        this.createdAt = post.getCreatedAt();
        this.nickname = post.getUser().getNickname();
        this.doneCount = post.getPostDoneList().size();
        this.agree = agree;
        this.agreeCount = post.getPostAgreeList().size();
        this.done = done;
        this.owner = owner;
        this.postDone = post.isDone();
        this.comments = comments.stream().map(comment -> new CommentResponseDto(comment, loginUser)).toList();
    }

    public PostResponseDto(Long postId) {
        this.postId = postId;
    }
}
