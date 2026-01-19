package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    Optional<Return> findByReturnNumber(String returnNumber);
    
    List<Return> findByOriginalBillId(Long billId);
    
    @Query("SELECT r FROM Return r WHERE r.returnDate BETWEEN :startDate AND :endDate")
    List<Return> findReturnsByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}





