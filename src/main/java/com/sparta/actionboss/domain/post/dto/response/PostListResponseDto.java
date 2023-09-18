package com.sparta.actionboss.domain.post.dto.response;

public record PostListResponseDto (
    Long postId,
    String title,
    Integer agreeCount,
    String nickname,
    String address,
    String thumbnail
) {

}