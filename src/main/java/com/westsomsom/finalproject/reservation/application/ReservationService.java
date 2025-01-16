package com.westsomsom.finalproject.reservation.application;

import com.westsomsom.finalproject.notification.application.NotificationService;
import com.westsomsom.finalproject.reservation.dao.ReservationRepository;
import com.westsomsom.finalproject.reservation.domain.Reservation;
import com.westsomsom.finalproject.reservation.domain.ReservationStatus;
import com.westsomsom.finalproject.store.application.StoreService;
import com.westsomsom.finalproject.store.domain.Store;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReservationRepository reservationRepository;
    private final StoreService storeService;
    private final NotificationService notificationService;
    private final ReservationPublisher reservationPublisher;

    private static final String REDIS_QUEUE_KEY = "reservationQueue|";
    private static final String UNIQUE_USERS_KEY = "uniqueUsers|";
    private static final String AVAILABLE_SLOTS_KEY = "availableSlots|";

    public boolean isUserInQueue(String queueKey, String userId) {
        List<Object> queue = redisTemplate.opsForList().range(queueKey, 0, -1);
        return queue != null && queue.contains(userId);
    }

    public boolean checkReservationTime(int storeId) {
        LocalDateTime now = LocalDateTime.now();
        Store store = storeService.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found for ID: " + storeId));
        return !now.isBefore(store.getReservationStart()) && !now.isAfter(store.getReservationFin());
    }

    public String joinQueue(String date, String timeSlot, String memberId, int storeId) {
        // 🚀 Key Hash Slot 강제 고정
        String queueKey = REDIS_QUEUE_KEY + "{" + storeId + "|" + date + "|" + timeSlot + "}";
        String uniqueUsersKey = UNIQUE_USERS_KEY + "{" + storeId + "|" + date + "|" + timeSlot + "}";
        String slotKey = AVAILABLE_SLOTS_KEY + "{" + storeId + "|" + date + "|" + timeSlot + "}";

        // Redis Set에서 사용자 중복 확인
        Boolean isAlreadyReserved = redisTemplate.opsForSet().isMember(uniqueUsersKey, memberId);
        if (Boolean.TRUE.equals(isAlreadyReserved)) {
            log.info("❌ [예약 확인] 사용자가 이미 예약 요청을 보냈습니다: {}", memberId);
            return "사용자가 이미 예약 요청을 보냈습니다: " + memberId;
        }

        // 대기열 중복 확인
        List<Object> queue = redisTemplate.opsForList().range(queueKey, 0, -1);
        if (queue != null && queue.contains(memberId)) {
            log.info("❌ [대기열 확인] 사용자가 이미 대기열에 있습니다: {}", memberId);
            return "사용자가 이미 대기열에 있습니다: " + memberId;
        }

        // 대기열 크기 제한 확인
        Long queueSize = redisTemplate.opsForList().size(queueKey);
        int maxQueueSize = 10000;
        if (queueSize != null && queueSize >= maxQueueSize) {
            log.info("❌ [대기열] 대기열이 가득 찼습니다.");
            return "대기열이 가득 찼습니다.";
        }

        // 예약 가능 슬롯 확인
        String slotValue = (String) redisTemplate.opsForValue().get(slotKey);
        int availableSlots = slotValue != null ? Integer.parseInt(slotValue) : 0;
        log.info("🔍 [예약 가능 슬롯 확인] 현재 슬롯 수: {}", availableSlots);

        if (availableSlots > 0) {
            Long addResult = redisTemplate.opsForSet().add(uniqueUsersKey, memberId);
            if (addResult > 0) {
                log.info("✅ [Redis] 사용자 '{}' 추가 완료! uniqueUsersKey: {}", memberId, uniqueUsersKey);
            } else {
                log.warn("🚨 [Redis] 사용자 '{}' 추가 실패! uniqueUsersKey: {}", memberId, uniqueUsersKey);
            }
        } else {
            log.warn("🚨 [예약 불가] 예약 가능 슬롯이 0입니다.");
            return "예약이 마감되었습니다.";
        }

        // Redis List 사용. 대기열에 사용자 추가
        redisTemplate.opsForList().rightPush(queueKey, memberId);
        log.info("✅ [대기열] 사용자 '{}'가 '{}' 대기열에 추가됨!", memberId, queueKey);

        // Pub/Sub 메시지 발행
        reservationPublisher.publishReservationEvent("reservationChannel",
                storeId + "|" + date + "|" + timeSlot + "|" + memberId);

        return "예약 성공";
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found for ID: " + reservationId));

        // 🚀 Key Hash Slot 강제 고정
        String slotKey = AVAILABLE_SLOTS_KEY + "{" + reservation.getStore().getStoreId() + "|" + reservation.getDate() + "|" + reservation.getTimeSlot() + "}";
        String uniqueUsersKey = UNIQUE_USERS_KEY + "{" + reservation.getStore().getStoreId() + "|" + reservation.getDate() + "|" + reservation.getTimeSlot() + "}";

        if (Boolean.TRUE.equals(redisTemplate.hasKey(slotKey))) {
            reservationRepository.updateStatus(reservationId, ReservationStatus.CANCELED);

            // 예약 가능 슬롯 증가
            String slotValue = (String) redisTemplate.opsForValue().get(slotKey);
            int availableSlots = slotValue != null ? Integer.parseInt(slotValue) : 0;
            availableSlots++;
            redisTemplate.opsForValue().set(slotKey, String.valueOf(availableSlots));

            // Redis에서 사용자 제거
            Long removedCount = redisTemplate.opsForSet().remove(uniqueUsersKey, reservation.getUser());
            if (removedCount > 0) {
                log.info("✅ [예약 취소] 사용자 '{}'가 Redis Set에서 제거됨.", reservation.getUser());
            } else {
                log.warn("🚨 [예약 취소] 사용자 '{}' 제거 실패! uniqueUsersKey: {}", reservation.getUser(), uniqueUsersKey);
            }
        } else {
            log.info("🚨 [예약 취소] 예약 취소 가능 기간이 아닙니다.");
        }
    }

    public List<Reservation> getReservationList(String user) {
        return reservationRepository.findByUser(user);
    }
}
