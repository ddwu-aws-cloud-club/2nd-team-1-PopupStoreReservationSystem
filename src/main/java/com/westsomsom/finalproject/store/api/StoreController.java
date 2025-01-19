package com.westsomsom.finalproject.store.api;

import com.westsomsom.finalproject.store.application.StoreService;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/api/search")
    public ResponseEntity<?> searchStore(@RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "start", required = false) LocalDate start,
                                         @RequestParam(value = "fin", required = false) LocalDate fin,
                                         @RequestParam(value = "loc", required = false) String loc,
                                         @RequestParam(value = "id", required = false) Integer storeId
    ) {

        SearchRequestDto searchRequestDto = SearchRequestDto.builder()
                .storeId(storeId)
                .storeName(name)
                .startDate(start)
                .finDate(fin)
                .storeLoc(loc)
                .build();

        List<SearchResponseDto> storeList = storeService.searchStore(searchRequestDto);

        return ResponseEntity.ok().body(storeList);
    }

    @GetMapping("/api/search/category")
    public ResponseEntity<?> searchStoreCategory(@RequestParam(value = "category", required = false) String category,
                                                 @RequestParam(value = "id", required = false) Integer storeId
    ) {
        List<SearchResponseDto> storeList = storeService.searchStoreCategory(category, storeId);
        return ResponseEntity.ok().body(storeList);
    }
}
