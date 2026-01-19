package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Batch;
import com.medicalstore.pos.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    
    List<Batch> findByMedicine(Medicine medicine);
    
    List<Batch> findByMedicineAndExpiryDateAfter(Medicine medicine, LocalDate date);
    
    // FIFO: Get batches ordered by expiry date (earliest first) with available stock
    @Query("SELECT b FROM Batch b WHERE b.medicine = :medicine " +
           "AND b.expiryDate > :currentDate " +
           "AND b.quantityAvailable > 0 " +
           "ORDER BY b.expiryDate ASC")
    List<Batch> findAvailableBatchesByMedicineOrderByExpiry(@Param("medicine") Medicine medicine, 
                                                             @Param("currentDate") LocalDate currentDate);
    
    // Lock batch for update (pessimistic locking for concurrent billing)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Batch b WHERE b.id = :batchId")
    Optional<Batch> findByIdForUpdate(@Param("batchId") Long batchId);
    
    // Find expired batches
    @Query("SELECT b FROM Batch b WHERE b.expiryDate < :currentDate AND b.quantityAvailable > 0")
    List<Batch> findExpiredBatches(@Param("currentDate") LocalDate currentDate);
    
    // Find low stock batches (quantity below threshold)
    @Query("SELECT b FROM Batch b WHERE b.quantityAvailable <= :threshold AND b.quantityAvailable > 0")
    List<Batch> findLowStockBatches(@Param("threshold") Integer threshold);
    
    // Get total available quantity for a medicine (non-expired batches only)
    @Query("SELECT COALESCE(SUM(b.quantityAvailable), 0) FROM Batch b " +
           "WHERE b.medicine = :medicine AND b.expiryDate > :currentDate")
    Integer getTotalAvailableQuantity(@Param("medicine") Medicine medicine, 
                                      @Param("currentDate") LocalDate currentDate);
    
    // Get total stock quantity for a medicine (all batches, including expired)
    @Query("SELECT COALESCE(SUM(b.quantityAvailable), 0) FROM Batch b " +
           "WHERE b.medicine = :medicine")
    Integer getTotalStockQuantity(@Param("medicine") Medicine medicine);
    
    // Get total stock quantity for multiple medicines (batch query for performance)
    @Query("SELECT b.medicine.id, COALESCE(SUM(b.quantityAvailable), 0) " +
           "FROM Batch b WHERE b.medicine.id IN :medicineIds " +
           "GROUP BY b.medicine.id")
    List<Object[]> getTotalStockQuantitiesByMedicineIds(@Param("medicineIds") List<Long> medicineIds);
    
    // Get all batches ordered by creation date (for purchase history)
    @Query("SELECT b FROM Batch b ORDER BY b.createdAt DESC")
    List<Batch> findAllOrderByCreatedAtDesc();
}

