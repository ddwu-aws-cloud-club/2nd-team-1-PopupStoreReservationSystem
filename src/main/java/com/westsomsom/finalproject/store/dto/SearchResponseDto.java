package com.westsomsom.finalproject.store.dto;

import com.westsomsom.finalproject.store.domain.Store;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponseDto {
    private String storeName;

    private String storeBio;

    private LocalDate startDate;

    private LocalDate finDate;

    private String storeCategory;

    private String storeLoc;

    public static SearchResponseDto toEntity(Store store) {
        return SearchResponseDto.builder()
                .storeName(store.getStoreName())
                .storeBio(store.getStoreBio())
                .startDate(store.getStartDate())
                .finDate(store.getFinDate())
                .storeCategory(store.getStoreCategory())
                .storeLoc(store.getStoreLoc())
                .build();
    }
}
