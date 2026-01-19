package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_items", indexes = {
    @Index(name = "idx_bill_item_bill", columnList = "bill_id"),
    @Index(name = "idx_bill_item_batch", columnList = "batch_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"bill", "medicine", "batch"})
@EqualsAndHashCode(exclude = {"bill", "medicine", "batch"})
public class BillItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bill_item_bill"))
    private Bill bill;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bill_item_medicine"))
    private Medicine medicine;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bill_item_batch"))
    private Batch batch;
    
    @Column(nullable = false, length = 50)
    private String batchNumber;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal gstPercentage;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal gstAmount;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
}


