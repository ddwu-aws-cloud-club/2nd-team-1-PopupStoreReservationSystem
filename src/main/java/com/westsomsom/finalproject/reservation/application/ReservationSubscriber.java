package com.westsomsom.finalproject.reservation.application;

import com.westsomsom.finalproject.reservation.dao.ReservationRepository;
import com.westsomsom.finalproject.reservation.domain.Reservation;
import com.westsomsom.finalproject.reservation.domain.ReservationStatus;
import com.westsomsom.finalproject.store.application.StoreService;
import com.westsomsom.finalproject.store.domain.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationSubscriber implements MessageListener {
    private final ReservationRepository reservationRepository;
    private final StoreService storeService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String receivedMessage = new String(message.getBody());
        log.info("Received message: {} from channel: {}", receivedMessage,channel);

        // 예약 처리 로직 실행
        processReservation(receivedMessage);
    }

    private void processReservation(String message) {
        try {
            // 메시지 파싱 (예: JSON 포맷일 경우 Jackson 사용)
            String[] parts = message.split("\\|"); // 예: "storeId|date|timeSlot|userId"
            int storeId = Integer.parseInt(parts[0]);
            String date = parts[1];
            String timeSlot = parts[2];
            String userId = parts[3];

            // 남은 슬롯 확인
            String slotKey = "availableSlots|" + storeId + "|" + date + "|" + timeSlot;
            String slotValue = (String) redisTemplate.opsForValue().get(slotKey);
            int availableSlots = slotValue != null ? Integer.parseInt(slotValue) : 0;

            if (availableSlots > 0) {
                // Store 정보 조회
                Store store = storeService.findById(storeId)
                        .orElseThrow(() -> new RuntimeException("Store not found for ID: " + storeId));

                // 예약 정보 생성 및 저장
                Reservation reservation = reservationRepository.save(Reservation.builder()
                        .store(store)
                        .date(date)
                        .timeSlot(timeSlot)
                        .user(userId)
                        .status(ReservationStatus.COMPLETED)
                        .build());

                log.info("예약 완료: 사용자 {}", userId);

                availableSlots--;
                redisTemplate.opsForValue().set(slotKey, String.valueOf(availableSlots));
                log.info("Updated available slots: {} for {}", availableSlots, slotKey);
            }else{
                log.info("예약이 마감되었습니다: 사용자 {}", userId);
                return;
            }

        } catch (Exception e) {
            log.error("Error processing reservation message: {}", message, e);
        }
    }
}