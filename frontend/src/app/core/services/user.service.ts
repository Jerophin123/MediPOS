import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ChangePasswordRequest {
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiService: ApiService) {}

  getAllUsers(): Observable<UserResponse[]> {
    return this.apiService.get<UserResponse[]>('/admin/users');
  }

  getUserById(userId: number): Observable<UserResponse> {
    return this.apiService.get<UserResponse>(`/admin/users/${userId}`);
  }

  changeUserPassword(userId: number, request: ChangePasswordRequest): Observable<UserResponse> {
    return this.apiService.put<UserResponse>(`/admin/users/${userId}/password`, request);
  }

  updateUserStatus(userId: number, active: boolean): Observable<UserResponse> {
    return this.apiService.put<UserResponse>(`/admin/users/${userId}/status?active=${active}`, {});
  }
}



