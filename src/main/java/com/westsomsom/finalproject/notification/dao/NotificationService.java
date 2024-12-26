package com.westsomsom.finalproject.notification.dao;

import com.westsomsom.finalproject.notification.dto.NotificationDto;
import com.westsomsom.finalproject.reservation.dao.ReservationRepository;
import com.westsomsom.finalproject.reservation.domain.Reservation;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.scheduler.SchedulerAsyncClient;
import software.amazon.awssdk.services.scheduler.model.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SchedulerAsyncClient schedulerAsyncClient;

    private final ReservationRepository reservationRepository;

    @Value("${custom.scheduler.targetArn}")
    private String targetArn;

    @Value("${custom.scheduler.roleArn}")
    private String roleArn;

    public CompletableFuture<Boolean> createScheduleAsync(Long reservationId) {
        // reservation 객체 찾기
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(RuntimeException::new);

        // 알림 이메일 Dto 생성
        NotificationDto notificationDto = NotificationDto.builder()
                .storeName(reservation.getStore().getStoreName())
                .date(reservation.getReservationDate().toString())
                .toEmail(reservation.getUser())
                .build();

        // Dto를 Json 형식으로 변환
        String input = createInput(notificationDto);

        // 스케줄 시간 설정 -> 현재 시간에서 10초 뒤로 설정
        String scheduleTime = Instant.now().plusSeconds(10)
                .atZone(java.time.ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        // 스케줄 생성 요청 생성
        CreateScheduleRequest request = buildCreateScheduleRequest(input, scheduleTime);

        // 비동기 요청 처리
        return schedulerAsyncClient.createSchedule(request)
                .thenApply(response -> true)
                .exceptionally(ex -> {
                    if (ex instanceof ConflictException) {
                        throw new CompletionException("Conflict occurred while creating schedule", ex);
                    } else {
                        throw new CompletionException("Error creating schedule", ex);
                    }
                });
    }

    private String createInput(NotificationDto notificationDto) {
        Map<String, Object> map = new HashMap<>();
        map.put("storeName", notificationDto.getStoreName());
        map.put("date", notificationDto.getDate());
        map.put("toEmail", notificationDto.getToEmail());

        return new JSONObject(map).toJSONString();
    }

    private CreateScheduleRequest buildCreateScheduleRequest(String input, String scheduleTime) {
        Target target = Target.builder()
                .arn(targetArn)
                .roleArn(roleArn)
                .input(input)
                .build();

        return CreateScheduleRequest.builder()
                .name(UUID.randomUUID().toString())
                .scheduleExpression("at(" + scheduleTime + ")")
                .groupName("default")
                .target(target)
                .actionAfterCompletion(ActionAfterCompletion.DELETE)
                .flexibleTimeWindow(FlexibleTimeWindow.builder()
                        .mode(FlexibleTimeWindowMode.OFF)
                        .build())
                .build();
    }
}
