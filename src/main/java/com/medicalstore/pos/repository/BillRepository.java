package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Bill;
import com.medicalstore.pos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillNumber(String billNumber);
    
    List<Bill> findByCashierAndBillDateBetween(User cashier, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT b FROM Bill b WHERE b.billDate BETWEEN :startDate AND :endDate")
    List<Bill> findBillsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Bill b WHERE b.cashier = :cashier AND b.billDate BETWEEN :startDate AND :endDate")
    List<Bill> findBillsByCashierAndDateRange(@Param("cashier") User cashier,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT MAX(CAST(SUBSTRING(b.billNumber, LENGTH(:prefix) + 1) AS long)) FROM Bill b WHERE b.billNumber LIKE CONCAT(:prefix, '%')")
    Long findMaxBillNumberSequence(@Param("prefix") String prefix);
    
    // Get all bills ordered by bill date (for purchase history)
    @Query("SELECT b FROM Bill b WHERE b.cancelled = false ORDER BY b.billDate DESC")
    List<Bill> findAllOrderByBillDateDesc();
}

