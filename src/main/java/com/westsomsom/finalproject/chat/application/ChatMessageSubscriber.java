package com.westsomsom.finalproject.chat.application;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageSubscriber implements MessageListener {
    private final ObjectMapper mapper;
    private final ChatRepository chatRepository;
    private final UserInfoRepository userInfoRepository;
    private final StoreRepository storeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
        String channel = new String(pattern);
        String receivedMessage = new String(message.getBody());

        log.info("Received message: {} from channel: {}", receivedMessage, channel);

        processMessage(receivedMessage);
    }

    private void processMessage(String receivedMessage) {
        try {
            log.debug("수신된 메시지를 JSON 트리로 파싱 중: {}", receivedMessage);
            JsonNode rootNode = mapper.readTree(receivedMessage);  // JSON을 JsonNode로 읽음

            // JSON 트리 구조를 로그로 출력
            log.debug("파싱된 JSON 트리: {}", rootNode);

            // 필요한 필드 추출
            JsonNode messageTypeNode = rootNode.get("messageType");
            JsonNode storeIdNode = rootNode.get("storeId");
            JsonNode senderNode = rootNode.get("sender");
            JsonNode messageNode = rootNode.get("message");

            log.debug("messageType: {}, storeId: {}, sender: {}, message: {}",
                    messageTypeNode.asText(),
                    storeIdNode.asInt(),
                    senderNode.asText(),
                    messageNode.asText());

            // 해당 값들이 잘 파싱되었는지 확인 후, ChatMessageDto로 역직렬화
            ChatMessageDto chatMessageDto = mapper.treeToValue(rootNode, ChatMessageDto.class);
            log.debug("역직렬화된 ChatMessageDto: {}", chatMessageDto);

            // Redis에 메시지 저장
            saveMessageToRedis(chatMessageDto);

            // 메시지를 DB에 저장
            // saveMessageToDB(chatMessageDto);
            log.info("Redis Subscriber 메시지 처리 완료: {}", chatMessageDto);
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            log.error("JSON 역직렬화 실패: {}", receivedMessage, e);
        } catch (Exception e) {
            log.error("Redis Subscriber 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void saveMessageToRedis(ChatMessageDto chatMessageDto) {
        try {
            // Redis에 저장할 키 생성: "Chat|<storeId>|<userId>|content"
            String redisKey = "Chat|" + chatMessageDto.getStoreId() + "|" + chatMessageDto.getSender() + "|"
                    + chatMessageDto.getMessage();

            // 메시지를 Redis에 저장
            redisTemplate.opsForValue().set(redisKey, chatMessageDto.getMessage());

            log.info("Redis에 채팅 메시지가 저장됨: key = {}", redisKey);
        } catch (Exception e) {
            log.error("Redis에 채팅 메시지를 저장하는 데 오류 발생: {}", e.getMessage(), e);
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
