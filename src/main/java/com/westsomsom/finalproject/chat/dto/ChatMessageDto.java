package com.westsomsom.finalproject.chat.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    // 메시지  타입 : 입장, 채팅, 퇴장
    public enum MessageType{
        JOIN, TALK, LEAVE
    }

    private MessageType messageType;
    private int storeId;
    private String sender;
    private String message;
}
