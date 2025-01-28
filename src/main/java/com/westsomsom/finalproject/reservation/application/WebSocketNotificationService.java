package com.westsomsom.finalproject.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendQueueUpdate(String userId, int position) {
        String destination = "/user/" + userId + "/queue-status";
        messagingTemplate.convertAndSend(destination,
                "사용자 " + userId + "의 현재 대기 순번: " + position);
    }
}