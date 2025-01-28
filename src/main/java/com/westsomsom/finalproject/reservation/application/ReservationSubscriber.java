package com.westsomsom.finalproject.reservation.application;

import com.westsomsom.finalproject.notification.application.NotificationService;
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
    private final NotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_QUEUE_KEY = "reservationQueue|";
    private static final String UNIQUE_USERS_KEY = "uniqueUsers|";
    private static final String AVAILABLE_SLOTS_KEY = "availableSlots|";

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
                String userId = parts3.replace("\"", "");

                String slotKey = "availableSlots|" + storeId + "|" + date + "|" + timeSlot;
                String slotValue = (String) redisTemplate.opsForValue().get(slotKey);
                int availableSlots = slotValue != null ? Integer.parseInt(slotValue) : 0;

                String queueKey = REDIS_QUEUE_KEY + storeId + "|" + date + "|" + timeSlot;
                String uniqueUsersKey = UNIQUE_USERS_KEY + storeId + "|" + date + "|" + timeSlot;

                Boolean uniqueUser = redisTemplate.opsForSet().isMember(uniqueUsersKey, userId);
                List<Object> queue = redisTemplate.opsForList().range(queueKey, 0, -1);

                if (queue != null && queue.contains(userId) && uniqueUser) {
                    Long queueRemovedCount = redisTemplate.opsForList().remove(queueKey, 0, userId);
                    if (queueRemovedCount > 0) {
                        log.info("사용자 '{}' 예약 진행", userId);
                    } else {
                        log.warn("사용자 '{}' 제거 실패! queueKey: {}", userId, queueKey);
                        break LOOP;
                    }

                    Store store = storeService.findById(storeId)
                            .orElseThrow(() -> new RuntimeException("Store not found for ID: " + storeId));

                    Reservation reservation = reservationRepository.save(Reservation.builder()
                            .store(store)
                            .date(date)
                            .timeSlot(timeSlot)
                            .user(userId)
                            .status(ReservationStatus.COMPLETED)
                            .build());

                    //notificationService.createScheduleAsync(id);

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