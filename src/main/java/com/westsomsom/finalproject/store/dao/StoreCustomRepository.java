package com.westsomsom.finalproject.store.dao;

import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreCustomRepository {
    Page<SearchResponseDto> searchStore(SearchRequestDto searchRequestDto, Pageable pageable);
    Page<SearchResponseDto> searchStoreCategory(String category, Pageable pageable);

}
