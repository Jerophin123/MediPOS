export interface ReturnItemRequest {
  billItemId: number;
  quantity: number;
}

export interface ReturnRequest {
  billId: number;
  reason: string;
  items: ReturnItemRequest[];
}



