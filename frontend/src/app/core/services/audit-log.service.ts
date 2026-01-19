import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface AuditLogResponse {
  id: number;
  userId: number;
  username: string;
  fullName: string;
  role: string;
  action: string;
  entityType: string;
  entityId: string;
  description: string;
  oldValue: string | null;
  newValue: string | null;
  timestamp: string;
  ipAddress: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuditLogService {
  constructor(private apiService: ApiService) {}

  getAllAuditLogs(): Observable<AuditLogResponse[]> {
    return this.apiService.get<AuditLogResponse[]>('/admin/audit/all');
  }

  getLoginLogoutLogs(): Observable<AuditLogResponse[]> {
    return this.apiService.get<AuditLogResponse[]>('/admin/audit/login-logout');
  }

  getAuditLogsByDateRange(startDate: string, endDate: string): Observable<AuditLogResponse[]> {
    return this.apiService.get<AuditLogResponse[]>(
      `/admin/audit/date-range?startDate=${startDate}&endDate=${endDate}`
    );
  }

  getAuditLogsByUser(userId: number): Observable<AuditLogResponse[]> {
    return this.apiService.get<AuditLogResponse[]>(`/admin/audit/user/${userId}`);
  }
}

