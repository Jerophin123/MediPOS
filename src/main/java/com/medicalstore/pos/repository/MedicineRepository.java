package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByHsnCode(String hsnCode);
    Optional<Medicine> findByBarcode(String barcode); // Barcode identifies medicine product
    List<Medicine> findByNameContainingIgnoreCase(String name);
    List<Medicine> findByStatus(Medicine.Status status);
    List<Medicine> findByCategory(String category);
    
    @Query("SELECT COUNT(b) FROM Batch b WHERE b.medicine.id = :medicineId")
    long countBatchesByMedicineId(@Param("medicineId") Long medicineId);
}

