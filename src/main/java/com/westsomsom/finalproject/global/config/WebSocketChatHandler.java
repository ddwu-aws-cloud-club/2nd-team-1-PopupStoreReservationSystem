package com.westsomsom.finalproject.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westsomsom.finalproject.chat.application.ChatMessagePublisher;
import com.westsomsom.finalproject.chat.application.ChatMessageSubscriber;
import com.westsomsom.finalproject.chat.application.ChatService;
import com.westsomsom.finalproject.chat.domain.Message;
import com.westsomsom.finalproject.chat.dto.ChatMessageDto;
import com.westsomsom.finalproject.store.application.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class WebSocketChatHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final StoreService storeService;
    private final ChatMessagePublisher chatMessagePublisher;
    private final ChatMessageSubscriber chatMessageSubscriber;

    // Store별 WebSocket 세션 관리
    private final Map<Integer, Set<WebSocketSession>> storeSessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("새로운 WebSocket 연결: {}", session.getId());

        int storeId = extractStoreIdFromSession(session);

        // Store 존재 여부 확인
        storeService.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));

        // 기존 메시지 클라이언트로 전송
        List<Message> messages = chatService.getMessagesByStoreId(storeId);
        for (Message message : messages) {
            String jsonMessage = objectMapper.writeValueAsString(convertMessageToDto(message));
            session.sendMessage(new TextMessage(jsonMessage));
        }

        // 세션 추가
        storeSessions.computeIfAbsent(storeId, k -> new HashSet<>()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        log.info("메시지 수신: {}", textMessage.getPayload());

        ChatMessageDto chatMessageDto = objectMapper.readValue(textMessage.getPayload(), ChatMessageDto.class);

        // ChatMessagePublisher를 통해 메시지를 Redis로 발행
        chatMessagePublisher.publish(textMessage.getPayload());

        // 동일 Store의 모든 세션에 메시지 브로드캐스트
        int storeId = chatMessageDto.getStoreId();
        Set<WebSocketSession> sessions = storeSessions.getOrDefault(storeId, Collections.emptySet());
        for (WebSocketSession wsSession : sessions) {
            if (wsSession.isOpen()) {
                wsSession.sendMessage(textMessage);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket 연결 종료: {}", session.getId());
        storeSessions.values().forEach(sessions -> sessions.remove(session));
    }

    private int extractStoreIdFromSession(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        return Integer.parseInt(Arrays.stream(query.split("&"))
                .filter(param -> param.startsWith("storeId="))
                .findFirst()
                .orElse("storeId=0")
                .split("=")[1]);
    }

    private ChatMessageDto convertMessageToDto(Message message) {
        return ChatMessageDto.builder()
                .messageType(ChatMessageDto.MessageType.TALK)
                .storeId(message.getStore().getStoreId())  // Store에서 getStoreId() 호출
                .sender(String.valueOf(message.getUserInfo().getUserId()))  // UserInfo에서 getUserId() 호출
                .message(message.getContent())
                .build();
    }
}
