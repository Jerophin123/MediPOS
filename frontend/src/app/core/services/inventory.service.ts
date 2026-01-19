import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiService } from './api.service';
import { Medicine, CreateMedicineRequest, UpdateMedicineRequest, MedicineStatus } from '../models/medicine.model';
import { Batch, CreateBatchRequest, UpdateBatchRequest, UpdateStockRequest } from '../models/batch.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  constructor(private apiService: ApiService) {}

  // Medicine methods
  getAllMedicines(): Observable<Medicine[]> {
    return this.apiService.get<Medicine[]>('/pharmacist/medicines');
  }

  getMedicineById(id: number): Observable<Medicine> {
    return this.apiService.get<Medicine>(`/pharmacist/medicines/${id}`);
  }

  searchMedicines(searchTerm: string): Observable<Medicine[]> {
    if (!searchTerm || searchTerm.trim().length < 2) {
      return throwError(() => new Error('Search term must be at least 2 characters'));
    }
    return this.apiService.get<Medicine[]>(`/pharmacist/medicines/search?name=${encodeURIComponent(searchTerm.trim())}`)
      .pipe(
        catchError(error => {
          console.error('Search error:', error);
          return throwError(() => error);
        })
      );
  }

  findMedicineByBarcode(barcode: string): Observable<Medicine> {
    if (!barcode || barcode.trim().length === 0) {
      return throwError(() => new Error('Barcode is required'));
    }
    return this.apiService.get<Medicine>(`/pharmacist/medicines/barcode/${encodeURIComponent(barcode.trim())}`)
      .pipe(
        catchError(error => {
          console.error('Barcode lookup error:', error);
          return throwError(() => error);
        })
      );
  }

  searchMedicinesByBarcode(prefix: string): Observable<Medicine[]> {
    if (!prefix || prefix.trim().length === 0) {
      return throwError(() => new Error('Barcode prefix is required'));
    }
    return this.apiService.get<Medicine[]>(`/pharmacist/medicines/barcode/search?prefix=${encodeURIComponent(prefix.trim())}`)
      .pipe(
        catchError(error => {
          console.error('Barcode search error:', error);
          return throwError(() => error);
        })
      );
  }

  createMedicine(request: CreateMedicineRequest): Observable<Medicine> {
    return this.apiService.post<Medicine>('/pharmacist/medicines', request);
  }

  updateMedicine(id: number, request: UpdateMedicineRequest): Observable<Medicine> {
    return this.apiService.put<Medicine>(`/pharmacist/medicines/${id}`, request);
  }

  deleteMedicine(id: number): Observable<void> {
    return this.apiService.delete<void>(`/pharmacist/medicines/${id}`);
  }

  updateMedicineStatus(id: number, status: MedicineStatus): Observable<Medicine> {
    return this.apiService.put<Medicine>(`/pharmacist/medicines/${id}/status?status=${status}`, {});
  }

  // Batch methods
  getBatchById(id: number): Observable<Batch> {
    return this.apiService.get<Batch>(`/pharmacist/batches/${id}`);
  }

  getBatchesByMedicine(medicineId: number): Observable<Batch[]> {
    return this.apiService.get<Batch[]>(`/pharmacist/batches/medicine/${medicineId}`);
  }

  getBatchByBarcode(barcode: string): Observable<Batch> {
    if (!barcode || barcode.trim().length === 0) {
      return throwError(() => new Error('Barcode is required'));
    }
    return this.apiService.get<Batch>(`/pharmacist/batches/barcode/${encodeURIComponent(barcode.trim())}`)
      .pipe(
        catchError(error => {
          console.error('Barcode batch lookup error:', error);
          return throwError(() => error);
        })
      );
  }

  createBatch(request: CreateBatchRequest): Observable<Batch> {
    return this.apiService.post<Batch>('/pharmacist/batches', request);
  }

  updateBatch(id: number, request: UpdateBatchRequest): Observable<Batch> {
    return this.apiService.put<Batch>(`/pharmacist/batches/${id}`, request);
  }

  updateStock(id: number, request: UpdateStockRequest): Observable<Batch> {
    return this.apiService.put<Batch>(`/pharmacist/batches/${id}/stock`, request);
  }

  deleteBatch(id: number): Observable<void> {
    return this.apiService.delete<void>(`/pharmacist/batches/${id}`);
  }

  getExpiredBatches(): Observable<Batch[]> {
    return this.apiService.get<Batch[]>('/pharmacist/batches/expired');
  }

  getLowStockBatches(threshold: number = 10): Observable<Batch[]> {
    return this.apiService.get<Batch[]>(`/pharmacist/batches/low-stock?threshold=${threshold}`);
  }

  getAllBatches(): Observable<Batch[]> {
    return this.apiService.get<Batch[]>('/pharmacist/batches');
  }

}

