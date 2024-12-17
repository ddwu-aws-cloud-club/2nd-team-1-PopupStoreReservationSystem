package com.westsomsom.finalproject.dao;

import com.westsomsom.finalproject.domain.Message;
import com.westsomsom.finalproject.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Message, Integer> {
    List<Message> findByStore(Store store);
}
