package com.westsomsom.finalproject.notification.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @Column(length = 100)
    private String notificationType;

    @Column(length = 100)
    private String content;

    private Date createdAt;
}
