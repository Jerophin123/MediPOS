import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ReturnRequest } from '../models/return.model';
import { BillResponse } from '../models/billing.model';

@Injectable({
  providedIn: 'root'
})
export class ReturnService {
  constructor(private apiService: ApiService) {}

  processReturn(request: ReturnRequest): Observable<BillResponse> {
    return this.apiService.post<BillResponse>('/cashier/returns', request);
  }
}

