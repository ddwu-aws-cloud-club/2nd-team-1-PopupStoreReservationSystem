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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {
    private final RedisTemplate redisTemplate;
    private final ReservationService reservationService;

    //예약 정보 생성 /api/reservation

    //예약 요청이 들어올 경우 -> 예약 가능 시간(오픈 시간 이후)이 맞는지 체크
    //만약 이전에 들어온 요청인 경우 -> "아직 예약 시간이 아닙니다"
    //예약 시간에 들어온 요청인 경우 -> 실시간 대기열 시스템에 추가
    //예약 시 인원수 같은 건x 그냥 시간대 선택만!!

    //날짜+시간대 별 예약 가능 인원 10명으로 설정

    //예약 신청 - 실시간 대기열 시스템

    //슬롯 초기화
    @GetMapping
    public void setSlot(@RequestParam String date,
                        @RequestParam String timeSlot,
                        @RequestParam int storeId){
        log.info("슬롯초기화 10");
        String slotKey = "availableSlots:"+ storeId + ":" + date + ":" + timeSlot;
        int availableSlots = 10;
        redisTemplate.opsForValue().set(slotKey, String.valueOf(availableSlots));
    }
    //대기열에 사용자 추가
    @PostMapping("/enter")
    public void enterQueue(@RequestBody ReservationDto reservationDto) {
        int storeId = reservationDto.getStoreId();
        String date = reservationDto.getDate();
        String timeSlot = reservationDto.getTimeSlot();

        String slotKey = "availableSlots:"+ storeId + ":" + date + ":" + timeSlot;

        if(!redisTemplate.hasKey(slotKey)){
            log.info("슬롯초기화 30");
            int availableSlots = 30;
            redisTemplate.opsForValue().set(slotKey, String.valueOf(availableSlots));
        }


        reservationService.joinQueue(reservationDto.getDate(), reservationDto.getTimeSlot(),reservationDto.getMemberId(),reservationDto.getStoreId());
        /*if(reservationService.checkReservationTime()) {
            log.info("예약이 가능합니다.");
            reservationService.joinQueue(date, timeSlot, userId);
            log.info("대기열에 추가되었습니다.");
        }else
            log.info("예약 시간이 아닙니다.");*/
    }

    @GetMapping("/queue-status")
    public void getQueueStatus(@RequestBody ReservationDto reservationDto) {
        reservationService.getQueueStatus(reservationDto.getDate(), reservationDto.getTimeSlot(), reservationDto.getMemberId(), reservationDto.getStoreId());
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

    //예약 취소(시작 시간 2시간 전까지만 가능)
    @PutMapping("/{reservationId}")
    public ResponseEntity<Boolean> cancelReservation(@PathVariable Long reservationId){
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(true);
    }
}
