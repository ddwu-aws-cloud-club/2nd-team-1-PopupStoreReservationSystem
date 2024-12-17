package com.westsomsom.finalproject.store.application;

import com.westsomsom.finalproject.store.dao.StoreRepository;
import com.westsomsom.finalproject.store.domain.Store;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    public Page<SearchResponseDto> searchStore(SearchRequestDto searchRequestDto, Pageable pageable) {
        return storeRepository.searchStore(searchRequestDto, pageable);
    }

    public Page<SearchResponseDto> searchStoreCategory(String category, Pageable pageable) {
        return storeRepository.searchStoreCategory(category, pageable);
    }
}
