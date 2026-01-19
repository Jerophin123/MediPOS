import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CreateBillRequest, BillResponse } from '../models/billing.model';

@Injectable({
  providedIn: 'root'
})
export class BillingService {
  constructor(private apiService: ApiService) {}

  createBill(request: CreateBillRequest): Observable<BillResponse> {
    return this.apiService.post<BillResponse>('/cashier/bills', request);
  }

  getBillById(id: number): Observable<BillResponse> {
    return this.apiService.get<BillResponse>(`/cashier/bills/${id}`);
  }

  getBillByBillNumber(billNumber: string): Observable<BillResponse> {
    return this.apiService.get<BillResponse>(`/cashier/bills/number/${billNumber}`);
  }

  cancelBill(id: number, reason: string): Observable<void> {
    return this.apiService.post<void>(`/cashier/bills/${id}/cancel?reason=${encodeURIComponent(reason)}`, {});
  }

  downloadBillPdf(billId: number): Observable<Blob> {
    return this.apiService.getBlob(`/cashier/bills/${billId}/pdf`);
  }

  getAllBills(): Observable<BillResponse[]> {
    return this.apiService.get<BillResponse[]>('/cashier/bills');
  }
}


