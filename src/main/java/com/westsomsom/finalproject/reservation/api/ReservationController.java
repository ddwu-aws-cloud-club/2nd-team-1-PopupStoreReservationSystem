package com.westsomsom.finalproject.reservation.api;

import com.westsomsom.finalproject.reservation.application.ReservationService;
import com.westsomsom.finalproject.reservation.domain.Reservation;
import com.westsomsom.finalproject.reservation.dto.ReservationDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {
    private final RedisTemplate redisTemplate;
    private final ReservationService reservationService;

    //대기열에 사용자 추가
    @PostMapping("/enter")
    public ResponseEntity<String> enterQueue(@RequestBody ReservationDto reservationDto) {
        int storeId = reservationDto.getStoreId();
        String date = reservationDto.getDate();
        String timeSlot = reservationDto.getTimeSlot();

        String slotKey = "availableSlots|"+ storeId + "|" + date + "|" + timeSlot;

        if(!redisTemplate.hasKey(slotKey)){
            if(!reservationService.checkReservationTime(storeId)){
                log.info("예약 시간이 아닙니다.");
                return ResponseEntity.ok("예약 시간이 아닙니다.");
            }
            log.info("슬롯초기화 10");
            int availableSlots = 10;
            redisTemplate.opsForValue().set(slotKey, String.valueOf(availableSlots));
        }

        String res = reservationService.joinQueue(reservationDto.getDate(), reservationDto.getTimeSlot(),reservationDto.getMemberId(),reservationDto.getStoreId());
        return ResponseEntity.ok(res);
    }

    /*@GetMapping("/queue-status")
    public ResponseEntity<String> getQueueStatus(
                                                 @RequestParam String memberId,
                                                 @RequestParam String date,
                                                 @RequestParam String timeSlot,
                                                 @RequestParam int storeId) {
        String res = reservationService.getQueueStatus(date, timeSlot, memberId, storeId);
        return ResponseEntity.ok(res);
    }*/

    //예약 목록 조회
    @GetMapping
    public ResponseEntity<List<Reservation>> getReservationList(@RequestParam String user){
        List<Reservation> reservationList = reservationService.getReservationList(user);
        return ResponseEntity.ok(reservationList);
    }

    //예약 조회 -> 마이페이지-예약 목록-상세 조회
    @GetMapping("/{reservationId}")
    public ResponseEntity<Reservation> getReservation(@PathVariable Long reservationId){
        return reservationService.getReservation(reservationId)
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());
    }

    //예약 내역 삭제
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Boolean> deleteReservation(@PathVariable Long reservationId){
        try {
            reservationService.deleteReservation(reservationId);

            return ResponseEntity.ok(true);
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(false);
        }
    }

    //예약 취소(예약 가능 기간 동안 가능)
    @PutMapping("/{reservationId}")
    public ResponseEntity<Boolean> cancelReservation(@PathVariable Long reservationId){
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(true);
    }
}
