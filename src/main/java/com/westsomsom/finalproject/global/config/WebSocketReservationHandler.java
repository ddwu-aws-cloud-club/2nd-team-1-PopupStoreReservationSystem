package com.westsomsom.finalproject.global.config;

import com.westsomsom.finalproject.reservation.application.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketReservationHandler extends TextWebSocketHandler {
    private final WebSocketNotificationService webSocketNotificationService;
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("✅ WebSocket 연결됨: {}", session.getId());

        // Ping 메시지 전송 (10초마다)
        new Thread(() -> {
            try {
                while (session.isOpen()) {
                    session.sendMessage(new PingMessage());
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                log.error("🚨 WebSocket Ping 메시지 전송 실패", e);
            }
        }).start();
    }



    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = message.getPayload();
        userSessions.put(userId, session);
        webSocketNotificationService.registerUserSession(userId, session);
        log.info("🔗 사용자 '{}' WebSocket 구독 완료", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userSessions.values().remove(session);
        log.info("❌ WebSocket 연결 종료: {}", session.getId());
    }
}