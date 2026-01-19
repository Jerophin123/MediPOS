import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { InventoryService } from '../../core/services/inventory.service';
import { DialogService } from '../../core/services/dialog.service';
import { Batch, UpdateBatchRequest, UpdateStockRequest } from '../../core/models/batch.model';

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss']
})
export class InventoryComponent implements OnInit {
  batches: Batch[] = [];
  expiredBatches: Batch[] = [];
  lowStockBatches: Batch[] = [];
  isLoading = false;
  showStockModal = false;
  showBatchModal = false;
  editingBatch: Batch | null = null;
  stockForm: FormGroup;
  batchForm: FormGroup;

  constructor(
    private inventoryService: InventoryService,
    private fb: FormBuilder,
    private dialogService: DialogService
  ) {
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
    this.loadExpiredBatches();
    this.loadLowStockBatches();
  }

  refreshAll(): void {
    this.loadExpiredBatches();
    this.loadLowStockBatches();
  }

  loadExpiredBatches(): void {
    this.isLoading = true;
    this.inventoryService.getExpiredBatches().subscribe({
      next: (batches) => {
        this.expiredBatches = batches;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading expired batches:', error);
        this.dialogService.error('Error loading expired batches: ' + (error.message || 'Unknown error'));
        this.isLoading = false;
      }
    });
  }

  loadLowStockBatches(): void {
    this.inventoryService.getLowStockBatches(10).subscribe({
      next: (batches) => {
        this.lowStockBatches = batches;
      },
      error: (error) => {
        console.error('Error loading low stock batches:', error);
        this.dialogService.error('Error loading low stock batches: ' + (error.message || 'Unknown error'));
      }
    });
  }

  trackByBatchId(index: number, batch: Batch): any {
    return batch.id || index;
  }

  openStockModal(batch: Batch): void {
    this.editingBatch = batch;
    this.stockForm.patchValue({
      quantityAvailable: batch.quantityAvailable
    });
    this.showStockModal = true;
  }

  closeStockModal(): void {
    this.showStockModal = false;
    this.editingBatch = null;
    this.stockForm.reset();
  }

  updateStock(): void {
    if (this.stockForm.invalid || !this.editingBatch) {
      return;
    }

    this.isLoading = true;
    const request: UpdateStockRequest = {
      quantityAvailable: this.stockForm.value.quantityAvailable
    };

    this.inventoryService.updateStock(this.editingBatch.id, request).subscribe({
      next: () => {
        this.refreshAll();
        this.closeStockModal();
        this.isLoading = false;
      },
      error: (error) => {
        this.dialogService.error(error.message || 'Error updating stock');
        this.isLoading = false;
      }
    });
  }

  openBatchModal(batch: Batch): void {
    this.editingBatch = batch;
    const expiryDate = new Date(batch.expiryDate).toISOString().split('T')[0];
    this.batchForm.patchValue({
      batchNumber: batch.batchNumber,
      expiryDate: expiryDate,
      purchasePrice: batch.purchasePrice,
      sellingPrice: batch.sellingPrice,
      quantityAvailable: batch.quantityAvailable
    });
    this.showBatchModal = true;
  }

  closeBatchModal(): void {
    this.showBatchModal = false;
    this.editingBatch = null;
    this.batchForm.reset();
  }

  updateBatch(): void {
    if (this.batchForm.invalid || !this.editingBatch) {
      return;
    }

    this.isLoading = true;
    const formValue = this.batchForm.value;
    const request: UpdateBatchRequest = {
      batchNumber: formValue.batchNumber,
      expiryDate: formValue.expiryDate,
      purchasePrice: formValue.purchasePrice,
      sellingPrice: formValue.sellingPrice,
      quantityAvailable: formValue.quantityAvailable
    };

    this.inventoryService.updateBatch(this.editingBatch.id, request).subscribe({
      next: () => {
        this.refreshAll();
        this.closeBatchModal();
        this.isLoading = false;
      },
      error: (error) => {
        this.dialogService.error(error.message || 'Error updating batch');
        this.isLoading = false;
      }
    });
  }

  async deleteBatch(batch: Batch): Promise<void> {
    const confirmed = await this.dialogService.confirm(
      `Are you sure you want to delete batch "${batch.batchNumber}"? This action cannot be undone.`,
      'Delete Batch'
    );
    if (!confirmed) {
      return;
    }

    this.isLoading = true;
    this.inventoryService.deleteBatch(batch.id).subscribe({
      next: () => {
        this.dialogService.success('Batch deleted successfully');
        this.refreshAll();
        this.isLoading = false;
      },
      error: (error) => {
        this.dialogService.error(error.message || 'Error deleting batch');
        this.isLoading = false;
      }
    });
  }
}

