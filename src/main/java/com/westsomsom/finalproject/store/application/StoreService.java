package com.westsomsom.finalproject.store.application;

import com.westsomsom.finalproject.store.dao.StoreRepository;
import com.westsomsom.finalproject.store.domain.Store;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import com.westsomsom.finalproject.store.dto.StoreRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.data.redis.cache.store}")
    private String cachePrefix; // Redis cache prefix for stores

    public Store createStore(StoreRequestDto storeRequestDto) {
        // Create a new store entity from the request DTO
        Store store = Store.builder()
                .storeName(storeRequestDto.getStoreName())
                .storeBio(storeRequestDto.getStoreBio())
                .startDate(storeRequestDto.getStartDate())
                .finDate(storeRequestDto.getFinDate())
                .storeCategory(storeRequestDto.getStoreCategory())
                .storeLoc(storeRequestDto.getStoreLoc())
                .reservationStart(storeRequestDto.getReservationStart())
                .reservationFin(storeRequestDto.getReservationFin())
                .build();
        log.info(store.getStoreName());
        Store savedStore = storeRepository.save(store);
        log.info(savedStore.getStoreId()+"");
        log.info("cachePrefix {}",cachePrefix);
        redisTemplate.opsForHash().put(cachePrefix, String.valueOf(savedStore.getStoreId()), savedStore);

        return savedStore;
    }

    public Page<Store> getAllStores(Pageable pageable) {
        return storeRepository.getAllStores(pageable);
    }

    public Page<SearchResponseDto> searchStore(SearchRequestDto searchRequestDto, Pageable pageable) {
        return storeRepository.searchStore(searchRequestDto, pageable);
    }

    public Page<SearchResponseDto> searchStoreCategory(String category, Pageable pageable) {
        return storeRepository.searchStoreCategory(category, pageable);
    }

    public Optional<Store> findById(int storeId){
        return storeRepository.findById(storeId);
    }
}
