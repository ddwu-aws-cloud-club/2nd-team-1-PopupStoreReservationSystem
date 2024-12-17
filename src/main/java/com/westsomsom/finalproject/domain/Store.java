package com.westsomsom.finalproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Store")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int storeId;

    @Column(nullable = false, length = 20)
    private String storeName;

    @Column(nullable = false, length = 100)
    private String storeBio;

    @Column(nullable = false)
    private Date startDate;

    @Column(nullable = false)
    private Date finDate;

    @Column(nullable = false, length = 20)
    private String storeCategory;

    @Column(nullable = false, length = 100)
    private String storeLoc;
}
