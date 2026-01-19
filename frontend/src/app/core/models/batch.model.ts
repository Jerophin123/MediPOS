export interface Batch {
  id: number;
  medicineId: number;
  medicineName: string;
  batchNumber: string;
  expiryDate: string;
  purchasePrice: number;
  sellingPrice: number;
  quantityAvailable: number;
  expired: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBatchRequest {
  medicineId: number;
  batchNumber: string;
  expiryDate: string;
  purchasePrice: number;
  sellingPrice: number;
  quantityAvailable: number;
}

export interface UpdateBatchRequest {
  batchNumber: string;
  expiryDate: string;
  purchasePrice: number;
  sellingPrice: number;
  quantityAvailable: number;
}

export interface UpdateStockRequest {
  quantityAvailable: number;
}

