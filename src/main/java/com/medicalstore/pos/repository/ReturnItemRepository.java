package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
}



