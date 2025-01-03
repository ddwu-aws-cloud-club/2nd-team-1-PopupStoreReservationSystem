package com.westsomsom.finalproject.store.api;

import com.westsomsom.finalproject.store.application.StoreService;
import com.westsomsom.finalproject.store.domain.Store;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/api/home")
    public ResponseEntity<Page<Store>> getAllStores(
            @RequestParam(value = "page", defaultValue = "0") Integer page
    ) {
        Page<Store> stores = storeService.getAllStores(PageRequest.of(page, 20));
        return ResponseEntity.ok(stores);
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
