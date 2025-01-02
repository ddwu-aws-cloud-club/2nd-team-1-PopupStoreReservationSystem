package com.westsomsom.finalproject.reservation.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReservationSubscriber implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String receivedMessage = new String(message.getBody());
        log.info("Received message: {} from channel: {}", receivedMessage,channel);

        // 예약 처리 로직 실행
        processReservation(receivedMessage);
    }

    private void processReservation(String message) {
        // 메시지를 기반으로 예약 처리 로직을 구현
        log.info("Processing reservation for: {}",message);
    }
}