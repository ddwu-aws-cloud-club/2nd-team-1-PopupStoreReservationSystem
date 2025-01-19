package com.westsomsom.finalproject.store.application;

import com.westsomsom.finalproject.store.dao.StoreRepository;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public List<SearchResponseDto> searchStore(SearchRequestDto searchRequestDto) {
        return storeRepository.searchStore(searchRequestDto);
    }

    public List<SearchResponseDto> searchStoreCategory(String category, Integer storeId) {
        return storeRepository.searchStoreCategory(category, storeId);
    }
}
