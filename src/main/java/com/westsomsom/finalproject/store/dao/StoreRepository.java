package com.westsomsom.finalproject.store.dao;

import com.westsomsom.finalproject.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Integer>, StoreCustomRepository {
}
