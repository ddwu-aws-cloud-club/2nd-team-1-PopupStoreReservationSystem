package com.westsomsom.finalproject.reservation.application;

import com.westsomsom.finalproject.reservation.dao.ReservationRepository;
import com.westsomsom.finalproject.reservation.domain.Reservation;
import com.westsomsom.finalproject.reservation.domain.ReservationStatus;
import com.westsomsom.finalproject.store.application.StoreService;
import com.westsomsom.finalproject.store.domain.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReservationRepository reservationRepository;
    private final StoreService storeService;

    private static final String REDIS_QUEUE_KEY = "reservationQueue:";
    private static final String UNIQUE_USERS_KEY = "uniqueUsers:";
    private static final String AVAILABLE_SLOTS_KEY = "availableSlots:";


    public boolean isUserInQueue(String queueKey, String userId) {
        List<Object> queue = redisTemplate.opsForList().range(queueKey, 0, -1);
        return queue != null && queue.contains(userId);
    }

    //예약 요청이 들어오면 예약 가능 시간인지 확인
    public boolean checkReservationTime(int storeId) {
        LocalDateTime now = LocalDateTime.now();
        Store store = storeService.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found for ID: " + storeId));
        //해당 팝업 예약 오픈 시간과 비교하게 수정
        if (now.isBefore(store.getReservationStart()) || now.isAfter(store.getReservationFin())) {
            return false;
        }
        return true;
    }

    //몇번째인지 확인
    public void joinQueue(String date, String timeSlot, String memberId, int storeId) {
        String queueKey = REDIS_QUEUE_KEY + storeId + ":" + date + ":" + timeSlot;
        String uniqueUsersKey = UNIQUE_USERS_KEY + storeId + ":" + date + ":" + timeSlot;
        // Redis Set에서 사용자 중복 확인
        Boolean isAlreadyReserved = redisTemplate.opsForSet().isMember(uniqueUsersKey, memberId);
        if (Boolean.TRUE.equals(isAlreadyReserved)) {
            log.info("사용자가 이미 예약 요청을 보냈습니다: {}", memberId);
            return;
        }

        // 대기열 중복 확인
        List<Object> queue = redisTemplate.opsForList().range(queueKey, 0, -1);
        if (queue != null && queue.contains(memberId)) {
            log.info("사용자가 이미 대기열에 있습니다: {}", memberId);
            return;
        }

        // 대기열 크기 확인
        if (queue != null && queue.size() >= 10) {
            log.info("대기열이 가득 찼습니다: {}", queueKey);
            return;
        }

        // Redis List 사용. 대기열에 사용자 추가
        redisTemplate.opsForList().rightPush(queueKey, memberId);
        redisTemplate.opsForSet().add(uniqueUsersKey, memberId);

        //대기열 만료 1시간
        redisTemplate.expire(queueKey, 1, TimeUnit.HOURS);
        redisTemplate.expire(uniqueUsersKey, 1, TimeUnit.HOURS);

        // 대기열에서 사용자 순서
        //Long position = redisTemplate.opsForList().size(queueKey);
        log.info("대기열에 추가되었습니다. 사용자 {} 현재 순번: {}", memberId, queue.size()+1);
    }

    //대기열 상태 확인
    public Map<String, Object> getQueueStatus(String date, String timeSlot, String memberId, int storeId) {
        String queueKey = REDIS_QUEUE_KEY + storeId + ":" + date + ":" + timeSlot;

        List<Object> queue = redisTemplate.opsForList().range(queueKey, 0, -1);

        if (queue == null || !queue.contains(memberId)) {
            log.info("대기열에 없습니다.");
            return null;
        }
        int position = queue.indexOf(memberId);
        int peopleBehind = queue.size() - position - 1;

        /*Map<String, Object> response = new HashMap<>();
        response.put("currentPosition", position + 1);
        response.put("peopleBehind", peopleBehind);*/

        log.info("사용자 {}는 현재 {}번째입니다. 뒤에 {}명 있습니다.", memberId, position+1, peopleBehind);

        return Map.of(
                "currentPosition", position + 1,
                "peopleBehind", peopleBehind,
                "queueSize", queue.size(),
                "userQueue", queue
        );
    }
    //예약 처리 스케줄러
    @Scheduled(fixedRate = 5000) // 5초마다 실행
    public void processQueue() {
        Set<String> availableSlotsKeys = redisTemplate.keys(AVAILABLE_SLOTS_KEY + "*");
        if (availableSlotsKeys == null || availableSlotsKeys.isEmpty()) {
            //log.info("예약 가능한 슬롯이 설정되지 않았습니다.");
            return;
        }

        for (String slotKey : availableSlotsKeys) {
            //String slotValue = (String) redisTemplate.opsForValue().get(slotKey);
            //int availableSlots = slotValue != null ? Integer.parseInt(slotValue) : 0;
            String[] keyParts = slotKey.split(":");
            int storeId = Integer.parseInt(keyParts[1]);
            String date = keyParts[2];
            String timeSlot = keyParts[3];
            String queueKey = slotKey.replace(AVAILABLE_SLOTS_KEY, REDIS_QUEUE_KEY);
            int availableSlots = Integer.parseInt((String) redisTemplate.opsForValue().get(slotKey));

            if(availableSlots > 0)
                while (availableSlots > 0) {
                    String nextUserId = (String) redisTemplate.opsForList().leftPop(queueKey);
                    if (nextUserId == null) break;

                    Store store = storeService.findById(storeId)
                            .orElseThrow(() -> new RuntimeException("Store not found for ID: " + storeId));

                    reservationRepository.save(Reservation.builder()
                            .date(date)
                            .timeSlot(timeSlot)
                            .user(nextUserId)
                            .status(ReservationStatus.COMPLETED)
                            .store(store)
                            .build());

                    availableSlots--;
                    redisTemplate.opsForValue().set(slotKey, String.valueOf(availableSlots));
                    log.info("예약 완료: 사용자 {} 남은 슬롯: {}", nextUserId, availableSlots);
                }
            else {
                if (redisTemplate.opsForList().size(queueKey) > 0) {
                    String nextUserId = (String) redisTemplate.opsForList().leftPop(queueKey);
                    if (nextUserId != null) {
                        log.info("예약이 마감되었습니다: 사용자 {}", nextUserId);
                    }
                }
            }
        }
    }

    //예약 정보 상세 조회
    public Optional<Reservation> getReservation(Long reservationId){
        //현재 로그인한 사용자 권한 확인
        return reservationRepository.findById(reservationId);
    }

    //예약 내역 삭제
    public void deleteReservation(Long reservationId){
        reservationRepository.deleteById(reservationId);
    }

    //예약 취소(내역에 남음)
    public void cancelReservation(Long reservationId) {
        reservationRepository.updateStatus(reservationId, ReservationStatus.CANCELED);
    }
}
