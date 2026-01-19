import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReportService } from '../../core/services/report.service';
import { DialogService } from '../../core/services/dialog.service';
import { SalesReportResponse, GstReportResponse, StockReportResponse } from '../../core/models/report.model';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent implements OnInit {
  reportForm: FormGroup;
  salesReport: SalesReportResponse | null = null;
  gstReport: GstReportResponse | null = null;
  stockReport: StockReportResponse | null = null;
  isLoading = false;
  activeTab: 'sales' | 'gst' | 'stock' = 'sales';

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private dialogService: DialogService
  ) {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    
    this.reportForm = this.fb.group({
      startDate: [this.formatDate(firstDay), Validators.required],
      endDate: [this.formatDate(today), Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.activeTab === 'stock') {
      this.loadStockReport();
    } else {
      this.loadSalesReport();
    }
  }

  formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  loadSalesReport(): void {
    if (this.reportForm.invalid) {
      return;
    }

    this.isLoading = true;
    const { startDate, endDate } = this.reportForm.value;

    this.reportService.getDailySalesReport(startDate, endDate).subscribe({
      next: (report) => {
        this.salesReport = report;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading sales report:', error);
        this.dialogService.error('Error loading sales report: ' + (error.message || 'Unknown error'));
        this.isLoading = false;
      }
    });
  }

  loadGstReport(): void {
    if (this.reportForm.invalid) {
      return;
    }

    this.isLoading = true;
    const { startDate, endDate } = this.reportForm.value;

    this.reportService.getGstReport(startDate, endDate).subscribe({
      next: (report) => {
        this.gstReport = report;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading GST report:', error);
        this.dialogService.error('Error loading GST report: ' + (error.message || 'Unknown error'));
        this.isLoading = false;
      }
    });
  }

  loadStockReport(): void {
    this.isLoading = true;
    this.reportService.getStockReport().subscribe({
      next: (report) => {
        this.stockReport = report;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading stock report:', error);
        this.dialogService.error('Error loading stock report: ' + (error.message || 'Unknown error'));
        this.isLoading = false;
      }
    });
  }

  onTabChange(tab: 'sales' | 'gst' | 'stock'): void {
    this.activeTab = tab;
    if (tab === 'sales') {
      this.loadSalesReport();
    } else if (tab === 'gst') {
      this.loadGstReport();
    } else if (tab === 'stock') {
      this.loadStockReport();
    }
  }

  trackByDate(index: number, daily: any): any {
    return daily.date || index;
  }

  trackByHsn(index: number, item: any): any {
    return item.hsnCode || index;
  }

  trackByMedicineId(index: number, item: any): any {
    return item.medicineId || index;
  }

  trackByBatchId(index: number, item: any): any {
    return item.batchId || index;
  }
}

