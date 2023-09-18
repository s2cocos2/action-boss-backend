package com.sparta.actionboss.domain.post.dto.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class PostRequestDto {
    private String title;
    private String content;
    private List<MultipartFile> images;
    private String address;
    private Double latitude;
    private Double longitude;
}
