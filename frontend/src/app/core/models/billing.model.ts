export interface BillItemRequest {
  medicineId?: number; // Optional: for regular medicine selection
  barcode?: string;    // Optional: GTIN/EAN barcode identifies medicine product
  quantity: number;     // Quantity to bill (any positive number)
}

export interface PaymentRequest {
  mode: PaymentMode;
  amount: number;
  paymentReference?: string;
}

export enum PaymentMode {
  CASH = 'CASH',
  UPI = 'UPI',
  CARD = 'CARD'
}

export interface CreateBillRequest {
  items: BillItemRequest[];
  customerName?: string;
  customerPhone?: string;
  payments: PaymentRequest[];
}

export interface BillItemResponse {
  id: number;
  medicineId: number;
  medicineName: string;
  batchNumber: string;
  quantity: number;
  unitPrice: number;
  gstPercentage: number;
  gstAmount: number;
  totalAmount: number;
}

export interface PaymentResponse {
  id: number;
  paymentReference: string;
  mode: PaymentMode;
  amount: number;
  status: PaymentStatus;
  paymentDate: string;
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

export interface BillResponse {
  id: number;
  billNumber: string;
  billDate: string;
  cashierId: number;
  cashierName: string;
  customerName?: string;
  customerPhone?: string;
  subtotal: number;
  totalGst: number;
  totalAmount: number;
  paymentStatus: BillPaymentStatus;
  cancelled: boolean;
  cancellationReason?: string;
  items: BillItemResponse[];
  payments: PaymentResponse[];
  createdAt: string;
}

export enum BillPaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  PARTIALLY_PAID = 'PARTIALLY_PAID',
  REFUNDED = 'REFUNDED'
}

