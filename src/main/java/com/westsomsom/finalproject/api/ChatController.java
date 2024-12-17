package com.westsomsom.finalproject.api;

import org.springframework.web.bind.annotation.RestController;
import com.westsomsom.finalproject.domain.Message;
import com.westsomsom.finalproject.application.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/{storeId}")
    public ResponseEntity<List<Message>> getMessagesByStore(@PathVariable int storeId) {
        List<Message> messages = chatService.getMessagesByStoreId(storeId);
        return ResponseEntity.ok(messages);
    }
}
