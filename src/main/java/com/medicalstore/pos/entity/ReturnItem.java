package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "return_items", indexes = {
    @Index(name = "idx_return_item_return", columnList = "return_id"),
    @Index(name = "idx_return_item_batch", columnList = "batch_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_item_return"))
    private Return returnEntity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_item_medicine"))
    private Medicine medicine;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_item_batch"))
    private Batch batch;
    
    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;
}





