package com.sparta.actionboss.domain.search.dto.response;

import lombok.Getter;

@Getter
public class SearchListResponseDto {
    private String address;
    private Double latitude;
    private Double longitude;

    public SearchListResponseDto(String address, Double latitude, Double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}