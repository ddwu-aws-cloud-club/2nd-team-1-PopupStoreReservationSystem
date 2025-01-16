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

import java.util.List;

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
        int maxAttempts = 5;
        int retryDelay = 100; // 시작 딜레이 (ms)

        LOOP:for (int attempts = 1; attempts <= maxAttempts; attempts++) {
            try {
                String[] parts = message.split("\\|");
                int storeId = Integer.parseInt(parts[0].replaceAll("[^0-9]", "").trim());
                String date = parts[1];
                String timeSlot = parts[2];
                String parts3 = parts[3];
                String userId = parts3.substring(0,parts[3].length()-1);

                String queueKey = "reservationQueue|" + storeId + "|" + date + "|" + timeSlot;
                List<Object> queue = redisTemplate.opsForList().range(queueKey, 0, -1);
                if (queue == null || !queue.contains(userId)) {
                    log.warn("🚨 [중복 방지] 사용자 '{}'의 예약이 이미 처리되었음. (queueKey 없음)", userId);
                    break LOOP;
                }

                String slotKey = "availableSlots|" + storeId + "|" + date + "|" + timeSlot;
                String slotValue = (String) redisTemplate.opsForValue().get(slotKey);
                int availableSlots = slotValue != null ? Integer.parseInt(slotValue) : 0;

                if (availableSlots > 0) {
                    Store store = storeService.findById(storeId)
                            .orElseThrow(() -> new RuntimeException("Store not found for ID: " + storeId));

                    Reservation reservation = reservationRepository.save(Reservation.builder()
                            .store(store)
                            .date(date)
                            .timeSlot(timeSlot)
                            .user(userId)
                            .status(ReservationStatus.COMPLETED)
                            .build());

                    log.info("예약 완료: 사용자 {}", userId);
                    redisTemplate.opsForValue().set(slotKey, String.valueOf(--availableSlots));
                    log.info("Updated available slots: {} for {}", availableSlots, slotKey);

                    break LOOP;
                } else {
                    log.info("예약이 마감되었습니다: 사용자 {}", userId);
                    break LOOP;
                }
            } catch (Exception e) {
                log.error("예약 처리 실패. 재시도 시도: {}/{}", attempts, maxAttempts, e);

                if (attempts == maxAttempts) {
                    log.error("최대 재시도 횟수 초과. 예약 처리 중단: {}", message);
                    break LOOP;
                }

                try {
                    Thread.sleep(retryDelay); // 딜레이 적용
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("재시도 중 인터럽트 발생.", ie);
                }

                retryDelay *= 2; // 지수 증가
            }
        }
    }
}