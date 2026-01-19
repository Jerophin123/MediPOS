package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Batch;
import com.medicalstore.pos.entity.StockBarcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockBarcodeRepository extends JpaRepository<StockBarcode, Long> {
    
    Optional<StockBarcode> findByBarcode(String barcode);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockBarcode s WHERE s.barcode = :barcode AND s.sold = false")
    Optional<StockBarcode> findAvailableByBarcode(@Param("barcode") String barcode);
    
    List<StockBarcode> findByBatch(Batch batch);
    
    List<StockBarcode> findByBatchAndSold(Batch batch, Boolean sold);
    
    @Query("SELECT COUNT(s) FROM StockBarcode s WHERE s.batch = :batch AND s.sold = false")
    long countAvailableByBatch(@Param("batch") Batch batch);
    
    /**
     * Search available barcodes by prefix (for incremental search)
     * Returns barcodes that match the prefix and are available
     */
    @Query("SELECT s FROM StockBarcode s " +
           "JOIN FETCH s.batch b " +
           "JOIN FETCH b.medicine m " +
           "WHERE UPPER(s.barcode) LIKE UPPER(CONCAT(:prefix, '%')) " +
           "AND s.sold = false " +
           "AND b.quantityAvailable > 0 " +
           "ORDER BY s.barcode")
    List<StockBarcode> findAvailableByBarcodePrefix(@Param("prefix") String prefix);
}

