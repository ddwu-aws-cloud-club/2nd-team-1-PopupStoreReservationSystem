package com.westsomsom.finalproject.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westsomsom.finalproject.dao.StoreRepository;
import com.westsomsom.finalproject.dao.UserInfoRepository;
import com.westsomsom.finalproject.dto.ChatMessageDto;
import com.westsomsom.finalproject.domain.Message;
import com.westsomsom.finalproject.dao.ChatRepository;
import com.westsomsom.finalproject.domain.UserInfo;
import com.westsomsom.finalproject.domain.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final ChatRepository chatRepository; // MessageRepository 의존성 주입
    private final UserInfoRepository userInfoRepository;
    private final StoreRepository storeRepository;

    // 팝업 스토어 ID와 세션을 매핑할 Map
    private final Map<Integer, Set<WebSocketSession>> popupStoreSessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("{} 연결됨", session.getId());
        session.sendMessage(new TextMessage("WebSocket 연결 완료"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("payload {}", payload);

        try {
            // 메시지를 ChatMessageDto로 변환
            ChatMessageDto chatMessageDto = mapper.readValue(payload, ChatMessageDto.class);
            log.info("messageDto {}", chatMessageDto);

            int popupStoreId = chatMessageDto.getStoreId();

            if (chatMessageDto.getMessageType().equals(ChatMessageDto.MessageType.JOIN)) {
                popupStoreSessions.computeIfAbsent(popupStoreId, s -> new HashSet<>()).add(session);
                chatMessageDto.setMessage(chatMessageDto.getSender() + " 님이 입장하셨습니다.");
            } else if (chatMessageDto.getMessageType().equals(ChatMessageDto.MessageType.LEAVE)) {
                popupStoreSessions.getOrDefault(popupStoreId, new HashSet<>()).remove(session);
                chatMessageDto.setMessage(chatMessageDto.getSender() + " 님이 퇴장하셨습니다.");
            } else if (chatMessageDto.getMessageType().equals(ChatMessageDto.MessageType.TALK)) {
                // 채팅 메시지 DB에 저장
                saveMessageToDB(chatMessageDto);
            }

            // 메시지 전송 (같은 팝업 스토어의 모든 세션에 전송)
            Set<WebSocketSession> sessions = popupStoreSessions.getOrDefault(popupStoreId, new HashSet<>());
            for (WebSocketSession webSocketSession : sessions) {
                webSocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(chatMessageDto)));
            }
        } catch (Exception e) {
            log.error("Error handling message: {}", e.getMessage(), e);
            session.sendMessage(new TextMessage("Error: " + e.getMessage()));
        }
    }

    private void saveMessageToDB(ChatMessageDto chatMessageDto) {
        // 실제 사용자 정보를 DB에서 조회해야 합니다
        UserInfo userInfo = userInfoRepository.findById(chatMessageDto.getSender())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 실제 스토어 정보를 DB에서 조회해야 합니다
        Store store = storeRepository.findById(chatMessageDto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Message message = new Message();
        message.setUserInfo(userInfo);
        message.setStore(store);
        message.setContent(chatMessageDto.getMessage());
        message.setTimestamp(java.time.LocalDateTime.now());

        chatRepository.save(message); // DB에 저장
        log.info("메시지 저장 완료: {}", message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("{} 연결 끊김", session.getId());
        popupStoreSessions.values().forEach(sessions -> sessions.remove(session));
    }
}
