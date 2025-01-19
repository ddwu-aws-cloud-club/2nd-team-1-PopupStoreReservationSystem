package com.westsomsom.finalproject.store.dao;

import com.westsomsom.finalproject.store.domain.Store;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface StoreCustomRepository {
    Slice<Store> findStoresNoOffset(Integer lastStoreId, Pageable pageable);
    Page<SearchResponseDto> searchStore(SearchRequestDto searchRequestDto, Pageable pageable);
    Page<SearchResponseDto> searchStoreCategory(String category, Pageable pageable);
//    Page<Store> getAllStores(Pageable pageable);
}
