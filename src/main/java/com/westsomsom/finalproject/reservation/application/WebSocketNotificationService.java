package com.westsomsom.finalproject.reservation.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WebSocketNotificationService {
    // 사용자 ID별 WebSocket 세션 관리
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // 사용자 WebSocket 세션 등록
    public void registerUserSession(String userId, WebSocketSession session) {
        userSessions.put(userId, session);
        log.info("✅ 사용자 '{}'의 WebSocket 세션 등록 완료", userId);
    }

    // 특정 사용자에게 대기 순번 메시지 전송
    public void sendQueueUpdate(String userId, int position) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("사용자 " + userId + "의 현재 대기 순번: " + position));
                log.info("📢 WebSocket 메시지 전송: 사용자 '{}', 순번 '{}'", userId, position);
            } catch (IOException e) {
                log.error("❌ WebSocket 메시지 전송 실패: 사용자 '{}'", userId, e);
            }
        }
    }

    // WebSocket 세션 제거
    public void removeUserSession(String userId) {
        userSessions.remove(userId);
        log.info("❌ 사용자 '{}'의 WebSocket 세션 제거 완료", userId);
    }
}