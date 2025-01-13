package com.westsomsom.finalproject.chat.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    // 메시지  타입 : 입장, 채팅, 퇴장
    @JsonFormat(shape = JsonFormat.Shape.STRING)  // enum을 문자열로 처리
    public enum MessageType{
        JOIN, TALK, LEAVE
    }

    private MessageType messageType; // 메시지 타입
    private int storeId; // 방번호
    private String sender; // 메시지 보낸 사람
    private String message; // 메시지

    // 추가적으로 @JsonCreator를 사용하여 JSON 문자열을 enum으로 변환할 수도 있습니다.
    @JsonCreator
    public static MessageType fromString(String value) {
        for (MessageType type : MessageType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + value);
    }
}
