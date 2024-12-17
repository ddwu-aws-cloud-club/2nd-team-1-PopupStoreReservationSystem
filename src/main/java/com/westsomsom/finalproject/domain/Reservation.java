package com.westsomsom.finalproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "Reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reservationId;

    @Column(nullable = false)
    private int reservationNumber;

    @Column(nullable = false)
    private Date reservationDate;

    @Column(nullable = false, length = 20)
    private String reservationState;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private UserInfo userInfo;

    @ManyToOne
    @JoinColumn(name = "storeId", nullable = false)
    private Store store;
}
