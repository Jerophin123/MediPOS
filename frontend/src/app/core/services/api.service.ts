import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl: string;

  constructor(private http: HttpClient) {
    // Dynamically determine API URL based on current hostname to support network access
    const hostname = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
    // If accessing via localhost or 127.0.0.1, use localhost
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      this.apiUrl = 'http://localhost:8080/api';
    } else {
      // Otherwise, use the current hostname (supports network IPs like 0.0.0.0, 192.168.x.x, etc.)
      this.apiUrl = `http://${hostname}:8080/api`;
    }
  }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  get<T>(endpoint: string): Observable<T> {
    return this.http.get<T>(`${this.apiUrl}${endpoint}`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError));
  }

  post<T>(endpoint: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.apiUrl}${endpoint}`, body, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError));
  }

  put<T>(endpoint: string, body: any): Observable<T> {
    return this.http.put<T>(`${this.apiUrl}${endpoint}`, body, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError));
  }

  delete<T>(endpoint: string): Observable<T> {
    return this.http.delete<T>(`${this.apiUrl}${endpoint}`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError));
  }

  getBlob(endpoint: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}${endpoint}`, {
      headers: this.getHeaders(),
      responseType: 'blob'
    }).pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      if (error.error?.message) {
        errorMessage = error.error.message;
      } else if (error.error?.error) {
        errorMessage = error.error.error;
        // If there are field errors, append them
        if (error.error.fieldErrors && Array.isArray(error.error.fieldErrors)) {
          const fieldErrors = error.error.fieldErrors
            .map((fe: any) => `${fe.field}: ${fe.message}`)
            .join('\n');
          errorMessage += '\n' + fieldErrors;
        }
      } else {
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      }
    }
    
    return throwError(() => new Error(errorMessage));
  }
}


