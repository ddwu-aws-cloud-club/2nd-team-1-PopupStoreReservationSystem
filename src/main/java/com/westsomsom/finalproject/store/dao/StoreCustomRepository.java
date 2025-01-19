package com.westsomsom.finalproject.store.dao;

import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;

import java.util.List;

public interface StoreCustomRepository {
    List<SearchResponseDto> searchStore(SearchRequestDto searchRequestDto);
    List<SearchResponseDto> searchStoreCategory(String category, Integer storeId);
}
