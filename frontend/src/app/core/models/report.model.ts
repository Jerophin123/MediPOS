export interface SalesReportResponse {
  startDate: string;
  endDate: string;
  totalBills: number;
  totalSales: number;
  totalGst: number;
  totalCash: number;
  totalUpi: number;
  totalCard: number;
  dailySales: DailySales[];
}

export interface DailySales {
  date: string;
  billCount: number;
  totalAmount: number;
}

export interface GstReportResponse {
  startDate: string;
  endDate: string;
  totalCgst: number;
  totalSgst: number;
  totalGst: number;
  gstBreakup: GstBreakup[];
}

export interface GstBreakup {
  hsnCode: string;
  medicineName: string;
  gstPercentage: number;
  taxableAmount: number;
  cgst: number;
  sgst: number;
  totalGst: number;
}

export interface StockReportResponse {
  reportDate: string;
  totalMedicines: number;
  totalBatches: number;
  totalStockQuantity: number;
  availableStockQuantity: number;
  expiredStockQuantity: number;
  lowStockMedicines: number;
  outOfStockMedicines: number;
  totalStockValue: number;
  medicineStock: MedicineStockItem[];
  expiredStock: ExpiredStockItem[];
  lowStockItems: LowStockItem[];
}

export interface MedicineStockItem {
  medicineId: number;
  medicineName: string;
  manufacturer: string;
  category?: string;
  hsnCode: string;
  totalStock: number;
  availableStock: number;
  expiredStock: number;
  lowStock: boolean;
  outOfStock: boolean;
  averageSellingPrice: number;
  stockValue: number;
}

export interface ExpiredStockItem {
  batchId: number;
  medicineId: number;
  medicineName: string;
  batchNumber: string;
  expiryDate: string;
  quantity: number;
  purchasePrice: number;
  stockValue: number;
}

export interface LowStockItem {
  medicineId: number;
  medicineName: string;
  manufacturer: string;
  availableStock: number;
  lowStockThreshold: number;
  averageSellingPrice: number;
}

