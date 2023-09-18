package com.sparta.actionboss.domain.mypage.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PagingResponseDto {
    private int totalPages;
    private int presentPage;
    private List<MyPagePostsResponseDto> content;

    public PagingResponseDto(Page<MyPagePostsResponseDto> page) {
        this.totalPages = page.getTotalPages();
        this.presentPage = page.getNumber();
        this.content = page.getContent();
    }

    public PagingResponseDto(int totalPages, int presentPage, List<MyPagePostsResponseDto> page) {
        this.totalPages = totalPages;
        this.presentPage = presentPage;
        this.content = page;
    }
}
