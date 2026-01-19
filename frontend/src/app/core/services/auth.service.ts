import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { LoginRequest, AuthResponse, User, UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'user_data';
  private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private apiService: ApiService) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.apiService.post<AuthResponse>('/auth/login', credentials)
      .pipe(
        tap(response => {
          this.setToken(response.token);
          const user: User = {
            id: response.userId,
            username: response.username,
            email: response.email || '',
            fullName: response.fullName || response.username,
            role: response.role as UserRole,
            active: true
          };
          this.setUser(user);
          this.currentUserSubject.next(user);
        })
      );
  }

  logout(): void {
    // Call backend to log logout event
    const user = this.getCurrentUser();
    if (user) {
      this.apiService.post('/auth/logout', {}).subscribe({
        next: () => {},
        error: () => {} // Ignore errors - logout should always succeed
      });
    }
    
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: UserRole): boolean {
    const user = this.getCurrentUser();
    return user?.role === role || user?.role === UserRole.ADMIN;
  }

  hasAnyRole(roles: UserRole[]): boolean {
    const user = this.getCurrentUser();
    if (!user) return false;
    return roles.includes(user.role) || user.role === UserRole.ADMIN;
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private setUser(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  private getStoredUser(): User | null {
    const userData = localStorage.getItem(this.USER_KEY);
    return userData ? JSON.parse(userData) : null;
  }
}



