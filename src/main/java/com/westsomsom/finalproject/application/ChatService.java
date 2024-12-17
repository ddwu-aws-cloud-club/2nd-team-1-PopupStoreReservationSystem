package com.westsomsom.finalproject.application;

import com.westsomsom.finalproject.dao.ChatRepository;
import com.westsomsom.finalproject.dao.StoreRepository;
import com.westsomsom.finalproject.domain.Message;
import com.westsomsom.finalproject.domain.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final StoreRepository storeRepository;

    public List<Message> getMessagesByStoreId(int storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));
        return chatRepository.findByStore(store);
    }
}
