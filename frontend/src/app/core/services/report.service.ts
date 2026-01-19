import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { SalesReportResponse, GstReportResponse, StockReportResponse } from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  constructor(private apiService: ApiService) {}

  getDailySalesReport(startDate: string, endDate: string): Observable<SalesReportResponse> {
    return this.apiService.get<SalesReportResponse>(
      `/admin/reports/sales?startDate=${startDate}&endDate=${endDate}`
    );
  }

  getGstReport(startDate: string, endDate: string): Observable<GstReportResponse> {
    return this.apiService.get<GstReportResponse>(
      `/admin/reports/gst?startDate=${startDate}&endDate=${endDate}`
    );
  }

  getCashierSalesReport(cashierId: number, startDate: string, endDate: string): Observable<SalesReportResponse> {
    return this.apiService.get<SalesReportResponse>(
      `/admin/reports/cashier/${cashierId}?startDate=${startDate}&endDate=${endDate}`
    );
  }

  getStockReport(): Observable<StockReportResponse> {
    return this.apiService.get<StockReportResponse>('/admin/reports/stock');
  }
}

