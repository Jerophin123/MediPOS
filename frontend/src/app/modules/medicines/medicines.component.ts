import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { InventoryService } from '../../core/services/inventory.service';
import { DialogService } from '../../core/services/dialog.service';
import { Medicine, CreateMedicineRequest, UpdateMedicineRequest } from '../../core/models/medicine.model';
import { Batch, CreateBatchRequest, UpdateStockRequest } from '../../core/models/batch.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-medicines',
  templateUrl: './medicines.component.html',
  styleUrls: ['./medicines.component.scss']
})
export class MedicinesComponent implements OnInit, OnDestroy {
  medicines: Medicine[] = [];
  medicineForm: FormGroup;
  showForm = false;
  isLoading = false;
  editingMedicine: Medicine | null = null;
  isEditMode = false;
  
  // Stock management
  showStockModal = false;
  managingMedicine: Medicine | null = null;
  batches: Batch[] = [];
  stockForm: FormGroup;
  batchForm: FormGroup;
  editingBatch: Batch | null = null;
  isEditingBatch = false;
  originalStockQuantity: number = 0; // Store original stock when opening update modal
  private stockQuantitySubscription?: Subscription;

  constructor(
    private fb: FormBuilder,
    private inventoryService: InventoryService,
    private dialogService: DialogService
  ) {
    this.medicineForm = this.fb.group({
      name: ['', Validators.required],
      manufacturer: ['', Validators.required],
      category: [''],
      barcode: [''], // GTIN/EAN - identifies product
      hsnCode: ['', Validators.required],
      gstPercentage: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
      prescriptionRequired: [false],
      // Optional: Initial stock and pricing
      initialStock: [null, [Validators.min(0)]],
      purchasePrice: [null, [Validators.min(0)]],
      sellingPrice: [null, [Validators.min(0)]],
      batchNumber: [''],
      expiryDate: ['']
    });

    // Stock management forms
    this.stockForm = this.fb.group({
      quantityAvailable: [0, [Validators.required, Validators.min(0)]]
    });

    this.batchForm = this.fb.group({
      batchNumber: ['', Validators.required],
      expiryDate: ['', Validators.required],
      purchasePrice: [0, [Validators.required, Validators.min(0)]],
      sellingPrice: [0, [Validators.required, Validators.min(0)]],
      quantityAvailable: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.loadMedicines();
  }

  get minDate(): string {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  }

  loadMedicines(): void {
    this.isLoading = true;
    this.inventoryService.getAllMedicines().subscribe({
      next: (medicines) => {
        this.medicines = medicines;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading medicines:', error);
        this.dialogService.error('Error loading medicines: ' + (error.message || 'Unknown error'));
        this.isLoading = false;
      }
    });
  }

  openForm(medicine?: Medicine): void {
    this.showForm = true;
    this.isEditMode = !!medicine;
    this.editingMedicine = medicine || null;
    if (medicine) {
      this.medicineForm.patchValue({
        name: medicine.name,
        manufacturer: medicine.manufacturer,
        category: medicine.category || '',
        barcode: medicine.barcode || '',
        hsnCode: medicine.hsnCode,
        gstPercentage: medicine.gstPercentage,
        prescriptionRequired: medicine.prescriptionRequired
      });
    } else {
      this.medicineForm.reset({
        prescriptionRequired: false,
        gstPercentage: 0
      });
    }
  }

  closeForm(): void {
    this.showForm = false;
    this.medicineForm.reset();
    this.editingMedicine = null;
    this.isEditMode = false;
  }


  onSubmit(): void {
    if (this.medicineForm.invalid) {
      // Mark all fields as touched to show validation errors
      Object.keys(this.medicineForm.controls).forEach(key => {
        this.medicineForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isLoading = true;
    
    if (this.isEditMode && this.editingMedicine) {
      const formValue = this.medicineForm.value;
      const request: UpdateMedicineRequest = {
        name: formValue.name,
        manufacturer: formValue.manufacturer,
        category: formValue.category || undefined,
        barcode: formValue.barcode || undefined,
        hsnCode: formValue.hsnCode,
        gstPercentage: formValue.gstPercentage,
        prescriptionRequired: formValue.prescriptionRequired
      };
      this.inventoryService.updateMedicine(this.editingMedicine.id, request).subscribe({
        next: () => {
          this.loadMedicines();
          this.closeForm();
          this.isLoading = false;
        },
        error: (error) => {
          this.dialogService.error(error.message || 'Error updating medicine');
          this.isLoading = false;
        }
      });
    } else {
      const formValue = this.medicineForm.value;
      const request: CreateMedicineRequest = {
        name: formValue.name,
        manufacturer: formValue.manufacturer,
        category: formValue.category || undefined,
        barcode: formValue.barcode || undefined,
        hsnCode: formValue.hsnCode,
        gstPercentage: formValue.gstPercentage,
        prescriptionRequired: formValue.prescriptionRequired,
        // Include stock and pricing if provided
        initialStock: formValue.initialStock && formValue.initialStock > 0 ? formValue.initialStock : undefined,
        purchasePrice: formValue.purchasePrice && formValue.purchasePrice > 0 ? formValue.purchasePrice : undefined,
        sellingPrice: formValue.sellingPrice && formValue.sellingPrice > 0 ? formValue.sellingPrice : undefined,
        batchNumber: formValue.batchNumber && formValue.batchNumber.trim() ? formValue.batchNumber : undefined,
        expiryDate: formValue.expiryDate || undefined
      };
      
      this.inventoryService.createMedicine(request).subscribe({
        next: () => {
          this.dialogService.success('Medicine created successfully!' + (request.initialStock ? ' Initial stock added.' : ''));
          this.loadMedicines();
          this.closeForm();
          this.isLoading = false;
        },
        error: (error) => {
          this.dialogService.error(error.message || 'Error creating medicine');
          this.isLoading = false;
        }
      });
    }
  }

  deleteMedicine(medicine: Medicine): void {
    // Delete confirmation will be handled in async function

    this.isLoading = true;
    this.inventoryService.deleteMedicine(medicine.id).subscribe({
      next: () => {
        this.loadMedicines();
        this.isLoading = false;
      },
      error: (error) => {
        this.dialogService.error(error.message || 'Error deleting medicine');
        this.isLoading = false;
      }
    });
  }

  trackByMedicineId(index: number, medicine: Medicine): any {
    return medicine.id || index;
  }

  // Stock Management Methods
  openStockModal(medicine: Medicine): void {
    this.managingMedicine = medicine;
    this.showStockModal = true;
    this.loadBatches(medicine.id);
    this.editingBatch = null;
    this.isEditingBatch = false;
    this.closeNewBatchModal(); // Ensure new batch modal is closed
  }

  closeStockModal(): void {
    this.showStockModal = false;
    this.managingMedicine = null;
    this.batches = [];
    this.stockForm.reset();
    this.batchForm.reset();
    this.editingBatch = null;
    this.isEditingBatch = false;
  }

  loadBatches(medicineId: number): void {
    this.isLoading = true;
    this.inventoryService.getBatchesByMedicine(medicineId).subscribe({
      next: (batches) => {
        this.batches = batches;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading batches:', error);
        this.dialogService.error('Error loading batches: ' + (error.message || 'Unknown error'));
        this.isLoading = false;
      }
    });
  }

  openUpdateStockModal(batch: Batch): void {
    this.editingBatch = batch;
    this.isEditingBatch = true;
    this.originalStockQuantity = batch.quantityAvailable; // Store original quantity
    this.stockForm.patchValue({
      quantityAvailable: batch.quantityAvailable
    });
  }

  closeUpdateStockModal(): void {
    // Unsubscribe from valueChanges to prevent memory leaks
    if (this.stockQuantitySubscription) {
      this.stockQuantitySubscription.unsubscribe();
      this.stockQuantitySubscription = undefined;
    }
    
    this.editingBatch = null;
    this.isEditingBatch = false;
    this.originalStockQuantity = 0;
    this.stockForm.reset();
  }

  ngOnDestroy(): void {
    if (this.stockQuantitySubscription) {
      this.stockQuantitySubscription.unsubscribe();
    }
  }

  updateStock(): void {
    if (this.stockForm.invalid || !this.editingBatch) {
      return;
    }

    const newQuantity = this.stockForm.value.quantityAvailable;
    const request: UpdateStockRequest = {
      quantityAvailable: newQuantity
    };

    this.isLoading = true;
    this.inventoryService.updateStock(this.editingBatch.id, request).subscribe({
      next: () => {
        if (this.managingMedicine) {
          this.loadBatches(this.managingMedicine.id);
          this.loadMedicines(); // Refresh medicines to update stock display
        }
        this.closeUpdateStockModal();
        this.isLoading = false;
      },
      error: (error) => {
        this.dialogService.error(error.message || 'Error updating stock');
        this.isLoading = false;
      }
    });
  }

  openNewBatchModal(): void {
    this.editingBatch = { id: 0 } as Batch; // Set a dummy value to show modal
    this.isEditingBatch = false;
    this.batchForm.reset({
      purchasePrice: 0,
      sellingPrice: 0,
      quantityAvailable: 0
    });
  }

  closeNewBatchModal(): void {
    this.editingBatch = null;
    this.isEditingBatch = false;
    this.batchForm.reset();
  }

  saveBatch(): void {
    if (this.batchForm.invalid || !this.managingMedicine) {
      Object.keys(this.batchForm.controls).forEach(key => {
        this.batchForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isLoading = true;
    const formValue = this.batchForm.value;

    const request: CreateBatchRequest = {
      medicineId: this.managingMedicine.id,
      batchNumber: formValue.batchNumber,
      expiryDate: formValue.expiryDate,
      purchasePrice: formValue.purchasePrice,
      sellingPrice: formValue.sellingPrice,
      quantityAvailable: formValue.quantityAvailable
    };

    this.inventoryService.createBatch(request).subscribe({
      next: () => {
        if (this.managingMedicine) {
          this.loadBatches(this.managingMedicine.id);
          this.loadMedicines(); // Refresh medicines to update stock display
        }
        this.closeNewBatchModal();
        this.isLoading = false;
      },
      error: (error) => {
        this.dialogService.error(error.message || 'Error creating batch');
        this.isLoading = false;
      }
    });
  }

  deleteBatch(batch: Batch): void {
    // Delete confirmation will be handled in async function

    this.isLoading = true;
    this.inventoryService.deleteBatch(batch.id).subscribe({
      next: () => {
        if (this.managingMedicine) {
          this.loadBatches(this.managingMedicine.id);
          this.loadMedicines(); // Refresh medicines to update stock display
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.dialogService.error(error.message || 'Error deleting batch');
        this.isLoading = false;
      }
    });
  }

  isExpired(expiryDate: string): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const expiry = new Date(expiryDate);
    expiry.setHours(0, 0, 0, 0);
    return expiry.getTime() < today.getTime();
  }

  isExpiringSoon(expiryDate: string): boolean {
    const today = new Date();
    const expiry = new Date(expiryDate);
    const diffTime = Math.abs(expiry.getTime() - today.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 30 && !this.isExpired(expiryDate);
  }

  isLowStock(quantity: number): boolean {
    return quantity > 0 && quantity <= 10;
  }

}

