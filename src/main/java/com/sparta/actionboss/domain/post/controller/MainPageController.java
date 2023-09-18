package com.sparta.actionboss.domain.post.controller;

import com.sparta.actionboss.domain.post.dto.response.MapListResponseDto;
import com.sparta.actionboss.domain.post.dto.response.PostListAndTotalPageResponseDto;
import com.sparta.actionboss.domain.post.dto.response.PostModalResponseDto;
import com.sparta.actionboss.domain.post.service.MainPageService;
import com.sparta.actionboss.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MainPageController {

    private final MainPageService mainPageService;

    @GetMapping("/main")
    public ResponseEntity<CommonResponse<PostListAndTotalPageResponseDto>> getPostList(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam Integer size,
            @RequestParam String sort,
            @RequestParam boolean isdone,
            @RequestParam Double northlatitude,
            @RequestParam Double eastlongitude,
            @RequestParam Double southlatitude,
            @RequestParam Double westlongitude
    ) {
        return new ResponseEntity<>(mainPageService.getPostList
                (page, size, sort, isdone, northlatitude, eastlongitude, southlatitude, westlongitude), HttpStatus.OK);
    }

    @GetMapping("/main/{postId}")
    public ResponseEntity<CommonResponse<PostModalResponseDto>> getSelectPost(@PathVariable Long postId) {
        return new ResponseEntity<>(mainPageService.getModalPost(postId), HttpStatus.OK);
    }

    @GetMapping("/main/map")
    public ResponseEntity<CommonResponse<List<MapListResponseDto>>> getMapList(
            @RequestParam boolean isdone,
            @RequestParam Double northlatitude,
            @RequestParam Double eastlongitude,
            @RequestParam Double southlatitude,
            @RequestParam Double westlongitude
    ) {
        return new ResponseEntity<>(mainPageService.getMapList(isdone, northlatitude, eastlongitude, southlatitude, westlongitude), HttpStatus.OK);
    }
}
