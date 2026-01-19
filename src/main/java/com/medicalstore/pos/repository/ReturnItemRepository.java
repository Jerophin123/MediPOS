package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Return;
import com.medicalstore.pos.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    @Query("SELECT ri FROM ReturnItem ri " +
           "LEFT JOIN FETCH ri.medicine " +
           "LEFT JOIN FETCH ri.batch " +
           "WHERE ri.returnEntity = :returnEntity")
    List<ReturnItem> findByReturnEntity(@Param("returnEntity") Return returnEntity);
}





