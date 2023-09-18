package com.sparta.actionboss.domain.search.service;

import com.sparta.actionboss.domain.search.dto.response.SearchListResponseDto;
import com.sparta.actionboss.domain.search.entity.Address;
import com.sparta.actionboss.domain.search.repository.SearchRepository;
import com.sparta.actionboss.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.sparta.actionboss.global.response.SuccessMessage.SEARCH_EMPTY;
import static com.sparta.actionboss.global.response.SuccessMessage.SEARCH_SUCCESS;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final SearchRepository searchRepository;

    public CommonResponse<List<SearchListResponseDto>> searchAddressList(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new CommonResponse<>(SEARCH_EMPTY, Collections.emptyList());
        }

        List<Address> searchAddressList = searchRepository.findByAddressContaining(keyword);

        if (searchAddressList == null || searchAddressList.isEmpty()) {
            return new CommonResponse<>(SEARCH_EMPTY, Collections.emptyList());
        }

        List<SearchListResponseDto> searchListResponseDto = searchAddressList.stream()
                .map(a -> new SearchListResponseDto(
                        a.getAddress(),
                        a.getLatitude(),
                        a.getLongitude()
                ))
                .collect(Collectors.toList());

        return new CommonResponse<>(SEARCH_SUCCESS, searchListResponseDto);
    }
}
