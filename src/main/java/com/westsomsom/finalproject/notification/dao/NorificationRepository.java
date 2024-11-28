package com.westsomsom.finalproject.notification.dao;

import com.westsomsom.finalproject.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NorificationRepository extends JpaRepository<Notification, Integer> {
}
