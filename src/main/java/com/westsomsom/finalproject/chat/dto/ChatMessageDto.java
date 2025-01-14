package com.westsomsom.finalproject.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    // 메시지  타입 : 입장, 채팅, 퇴장
    @JsonFormat(shape = JsonFormat.Shape.STRING) // enum을 String으로 직렬화
    public enum MessageType{
        JOIN, TALK, LEAVE
    }

    private MessageType messageType; // 메시지 타입
    private int storeId; // 방번호
    private String sender; // 메시지 보낸 사람
    private String message; // 메시지
}
