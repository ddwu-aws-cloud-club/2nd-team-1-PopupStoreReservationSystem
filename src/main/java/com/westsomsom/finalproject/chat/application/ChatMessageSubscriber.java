package com.westsomsom.finalproject.chat.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westsomsom.finalproject.chat.dao.ChatRepository;
import com.westsomsom.finalproject.chat.domain.Message;
import com.westsomsom.finalproject.chat.dto.ChatMessageDto;
import com.westsomsom.finalproject.store.dao.StoreRepository;
import com.westsomsom.finalproject.store.domain.Store;
import com.westsomsom.finalproject.user.dao.UserInfoRepository;
import com.westsomsom.finalproject.user.domain.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageSubscriber implements MessageListener {
    private final ObjectMapper mapper;
    private final ChatRepository chatRepository;
    private final UserInfoRepository userInfoRepository;
    private final StoreRepository storeRepository;

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
        String channel = new String(pattern);
        String receivedMessage = new String(message.getBody());
        log.info("Received message: {} from channel: {}", receivedMessage, channel);

        try {
            // Redis에서 전달받은 메시지를 처리
            ChatMessageDto chatMessageDto = mapper.readValue(receivedMessage, ChatMessageDto.class);

            // DB 저장
            saveMessageToDB(chatMessageDto);
            log.info("Redis Subscriber 메시지 처리 완료: {}", chatMessageDto);
        } catch (Exception e) {
            log.error("Redis Subscriber 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void saveMessageToDB(ChatMessageDto chatMessageDto) {
        UserInfo userInfo = userInfoRepository.findById(chatMessageDto.getSender())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Store store = storeRepository.findById(chatMessageDto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Message message = new Message();
        message.setUserInfo(userInfo);
        message.setStore(store);
        message.setContent(chatMessageDto.getMessage());
        message.setTimestamp(java.time.LocalDateTime.now());

        chatRepository.save(message);
    }
}
