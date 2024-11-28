package com.westsomsom.finalproject.store.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer storeId;

    @Column(length = 20)
    private String storeName;

    private String storeBio;

    private LocalDate startDate;

    private LocalDate finDate;

    @Column(length = 20)
    private String storeCategory;

    @Column(length = 100)
    private String storeLoc;

    @Builder
    public Store(String storeName, String storeBio, LocalDate startDate, LocalDate finDate, String storeCategory, String storeLoc) {
        this.storeName = storeName;
        this.storeBio = storeBio;
        this.startDate = startDate;
        this.finDate = finDate;
        this.storeCategory = storeCategory;
        this.storeLoc = storeLoc;
    }
}
