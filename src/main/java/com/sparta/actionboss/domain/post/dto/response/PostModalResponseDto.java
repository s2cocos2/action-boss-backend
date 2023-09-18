package com.sparta.actionboss.domain.post.dto.response;

import com.sparta.actionboss.domain.post.entity.Post;
import lombok.Getter;

@Getter
public class PostModalResponseDto {
    private Long postId;
    private String title;
    private String thumbnail;
    private String address;
    private String nickname;
    private Integer agreeCount;
    private Boolean done;

    public PostModalResponseDto(Post post,String imageUrl) {
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.thumbnail = imageUrl;
        this.address = post.getAddress();
        this.nickname = post.getUser().getNickname();
        this.agreeCount = post.getAgreeCount();
        this.done = post.isDone();
    }
}
