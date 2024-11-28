package com.westsomsom.finalproject.store.api;

import com.westsomsom.finalproject.store.application.StoreService;
import com.westsomsom.finalproject.store.dto.SearchRequestDto;
import com.westsomsom.finalproject.store.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Date;
import java.util.logging.SimpleFormatter;

@RestController
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

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

        Page<SearchResponseDto> storeList = storeService.searchStore(searchRequestDto, PageRequest.of(page,10));

        return ResponseEntity.ok().body(storeList.getContent());
    }
}
