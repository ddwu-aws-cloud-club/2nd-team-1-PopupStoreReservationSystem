package com.westsomsom.finalproject.store.api;

import com.westsomsom.finalproject.store.application.StoreService;
import com.westsomsom.finalproject.store.domain.Store;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import com.westsomsom.finalproject.store.dto.StoreRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class StoreController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StoreService storeService;
    @Value("${spring.data.redis.cache.store}")
    private String cachePrefix; // Redis cache prefix for stores

    @PostMapping("/api/store")
    public ResponseEntity<Store> createStore(@RequestBody StoreRequestDto storeRequestDto) {
        Store store = storeService.createStore(storeRequestDto);
        return ResponseEntity.ok(store);
    }

//    @GetMapping("/api/home")
//    public ResponseEntity<Page<Store>> getAllStores(
//            @RequestParam(value = "page", defaultValue = "0") Integer page
//    ) {
//        Page<Store> stores = storeService.getAllStores(PageRequest.of(page, 20));
//        return ResponseEntity.ok(stores);
//    }

    //@Cacheable(value = "stores", key = "#page")
    @GetMapping("/api/home")
    public ResponseEntity<List<Store>> getAllStores(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        List<Store> storeList = new ArrayList<>();

        // Redis에서 전체 데이터를 조회
        Map<Object, Object> allStores = redisTemplate.opsForHash().entries(cachePrefix);

        if (allStores.isEmpty()) {
            // Redis에 데이터가 없을 경우: DB에서 조회
            Page<Store> stores = storeService.getAllStores(PageRequest.of(page, size));
            storeList = stores.getContent();

            // Redis에 저장
            stores.getContent().forEach(store -> {
                redisTemplate.opsForHash().put(cachePrefix, String.valueOf(store.getStoreId()), store);
            });
        } else {
            // Redis에서 데이터를 가져와 페이징 처리
            List<Store> allStoresList = allStores.values().stream()
                    .map(obj -> (Store) obj)
                    .sorted(Comparator.comparing(Store::getStoreId)) // 정렬이 필요하다면
                    .collect(Collectors.toList());

            int start = page * size;
            int end = Math.min(start + size, allStoresList.size());

            if (start < allStoresList.size()) {
                storeList = allStoresList.subList(start, end);
            }
        }

        return ResponseEntity.ok(storeList);
    }


    @GetMapping("/api/search")
    public ResponseEntity<?> searchStore(@RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "start", required = false) LocalDate start,
                                         @RequestParam(value = "fin", required = false) LocalDate fin,
                                         @RequestParam(value = "loc", required = false) String loc,
                                         @RequestParam(value = "page", defaultValue = "0") Integer page
    ) {

        SearchRequestDto searchRequestDto = SearchRequestDto.builder()
                .storeName(name)
                .startDate(start)
                .finDate(fin)
                .storeLoc(loc)
                .build();

        Page<SearchResponseDto> storeList = storeService.searchStore(searchRequestDto, PageRequest.of(page,20));

        return ResponseEntity.ok().body(storeList.getContent());
    }

    @GetMapping("/api/search/category")
    public ResponseEntity<?> searchStoreCategory(@RequestParam(value = "category", required = false) String category,
                                                 @RequestParam(value = "page", defaultValue = "0") Integer page
    ) {
        Page<SearchResponseDto> storeList = storeService.searchStoreCategory(category, PageRequest.of(page,20));
        return ResponseEntity.ok().body(storeList.getContent());
    }

    //팝업 스토어 상세정보
    @GetMapping("/api/store/{storeId}")
    public ResponseEntity<Store> getStoreById(@PathVariable("storeId") int storeId) {
        Optional<Store> store = storeService.findById(storeId);

        if (store.isPresent()) {
            return ResponseEntity.ok(store.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
