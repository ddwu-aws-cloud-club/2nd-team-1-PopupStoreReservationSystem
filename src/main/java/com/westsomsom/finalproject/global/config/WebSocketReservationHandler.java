package com.westsomsom.finalproject.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketReservationHandler extends TextWebSocketHandler {
    // 사용자 ID와 WebSocket 세션 매핑
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("✅ WebSocket 연결됨: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = message.getPayload();
        userSessions.put(userId, session);
        log.info("🔗 사용자 '{}' WebSocket 구독 완료", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userSessions.values().remove(session);
        log.info("❌ WebSocket 연결 종료: {}", session.getId());
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
}
