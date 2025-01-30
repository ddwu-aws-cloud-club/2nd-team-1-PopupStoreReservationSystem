package com.westsomsom.finalproject.store.dto;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequestDto {
    private Integer storeId;

    private String storeName;

    private LocalDate startDate;

    private LocalDate finDate;

    private String storeLoc;
}
