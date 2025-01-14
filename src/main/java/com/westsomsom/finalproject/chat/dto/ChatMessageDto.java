package com.westsomsom.finalproject.chat.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
//@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    // 메시지  타입 : 입장, 채팅, 퇴장
    public enum MessageType{
        JOIN, TALK, LEAVE
    }

    private MessageType messageType; // 메시지 타입
    private int storeId; // 방번호
    private String sender; // 메시지 보낸 사람
    private String message; // 메시지

    @JsonCreator
    public ChatMessageDto(@JsonProperty("messageType") MessageType messageType,
                          @JsonProperty("storeId") int storeId,
                          @JsonProperty("sender") String sender,
                          @JsonProperty("message") String message) {
        this.messageType = messageType;
        this.storeId = storeId;
        this.sender = sender;
        this.message = message;
    }
}
