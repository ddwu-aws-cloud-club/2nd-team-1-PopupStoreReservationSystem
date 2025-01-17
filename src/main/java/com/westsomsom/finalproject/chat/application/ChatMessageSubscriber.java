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

        processMessage(receivedMessage);
    }

    private void processMessage(String receivedMessage) {
        try {
            log.debug("수신된 메시지를 ChatMessageDto로 역직렬화하려고 시도하는 중: {}", receivedMessage);
            ChatMessageDto chatMessageDto = mapper.readValue(receivedMessage, ChatMessageDto.class);
            log.debug("역직렬화된 ChatMessageDto: {}", chatMessageDto);

            // 메시지를 DB에 저장
            // saveMessageToDB(chatMessageDto);
            log.info("Redis Subscriber 메시지 처리 완료: {}", chatMessageDto);
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            log.error("JSON 역직렬화 실패: {}", receivedMessage, e);
        } catch (Exception e) {
            log.error("Redis Subscriber 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void saveMessageToDB(ChatMessageDto chatMessageDto) {
        try {
            // senderId를 String으로 처리
            String senderId = chatMessageDto.getSender();

            UserInfo userInfo = userInfoRepository.findById(senderId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + senderId));

            Store store = storeRepository.findById(chatMessageDto.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store not found with id: " + chatMessageDto.getStoreId()));

            // Message 객체 생성
            Message message = Message.builder()
                    .userInfo(userInfo)
                    .content(chatMessageDto.getMessage())
                    .store(store)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            // DB 저장
            chatRepository.save(message);

            log.info("Message saved to DB: {}", message);
        } catch (Exception e) {
            log.error("Error saving message to DB: {}", e.getMessage(), e);
        }
    }
}
