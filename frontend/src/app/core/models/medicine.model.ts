export interface Medicine {
  id: number;
  name: string;
  manufacturer: string;
  category?: string;
  barcode?: string;              // GTIN/EAN - identifies product, NOT unique per unit
  hsnCode: string;
  gstPercentage: number;
  prescriptionRequired: boolean;
  status: MedicineStatus;
  // Real-time stock information
  totalStock?: number;           // Total stock across all batches
  availableStock?: number;        // Stock in non-expired batches only
  lowStock?: boolean;             // True if stock is below threshold
  outOfStock?: boolean;           // True if no available stock
  lowStockThreshold?: number;      // Threshold for low stock alert
  createdAt: string;
  updatedAt: string;
}

export enum MedicineStatus {
  ACTIVE = 'ACTIVE',
  DISCONTINUED = 'DISCONTINUED'
}

export interface CreateMedicineRequest {
  name: string;
  manufacturer: string;
  category?: string;
  barcode?: string;              // GTIN/EAN - identifies product
  hsnCode: string;
  gstPercentage: number;
  prescriptionRequired: boolean;
  // Optional: Initial stock and pricing (creates batch automatically)
  initialStock?: number;
  purchasePrice?: number;
  sellingPrice?: number;
  batchNumber?: string;
  expiryDate?: string; // ISO date string (YYYY-MM-DD)
}

export interface UpdateMedicineRequest {
  name: string;
  manufacturer: string;
  category?: string;
  barcode?: string;              // GTIN/EAN - identifies product
  hsnCode: string;
  gstPercentage: number;
  prescriptionRequired: boolean;
}

