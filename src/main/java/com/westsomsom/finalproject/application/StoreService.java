package com.westsomsom.finalproject.application;

import com.westsomsom.finalproject.dao.StoreRepository;
import com.westsomsom.finalproject.domain.Store;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }
}
