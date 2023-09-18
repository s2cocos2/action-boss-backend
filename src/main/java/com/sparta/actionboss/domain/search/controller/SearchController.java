package com.sparta.actionboss.domain.search.controller;

import com.sparta.actionboss.domain.search.dto.response.SearchListResponseDto;
import com.sparta.actionboss.domain.search.service.SearchService;
import com.sparta.actionboss.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<SearchListResponseDto>>> searchAddressList(@RequestParam String keyword) {
        return new ResponseEntity<>(searchService.searchAddressList(keyword), HttpStatus.OK);
    }
}
