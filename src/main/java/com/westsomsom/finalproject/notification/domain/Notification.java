package com.westsomsom.finalproject.notification.domain;

import com.westsomsom.finalproject.reservation.domain.Reservation;
import com.westsomsom.finalproject.user.domain.UserInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "Notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int notificationId;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private UserInfo userInfo;

    @ManyToOne
    @JoinColumn(name = "reservationId", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, length = 20)
    private String notificationType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Date createdAt;
}
