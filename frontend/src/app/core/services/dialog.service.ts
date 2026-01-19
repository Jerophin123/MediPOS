import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { DialogData } from '../components/dialog/dialog.component';

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  private dialogState$ = new BehaviorSubject<DialogData | null>(null);

  get dialogState(): Observable<DialogData | null> {
    return this.dialogState$.asObservable();
  }

  show(data: DialogData): void {
    this.dialogState$.next(data);
  }

  close(): void {
    this.dialogState$.next(null);
  }

  // Convenience methods
  alert(message: string, title?: string): Promise<void> {
    return new Promise((resolve) => {
      this.show({
        title: title || 'Alert',
        message,
        type: 'info',
        onConfirm: () => {
          resolve();
        }
      });
    });
  }

  success(message: string, title?: string): Promise<void> {
    return new Promise((resolve) => {
      this.show({
        title: title || 'Success',
        message,
        type: 'success',
        onConfirm: () => {
          resolve();
        }
      });
    });
  }

  error(message: string, title?: string): Promise<void> {
    return new Promise((resolve) => {
      this.show({
        title: title || 'Error',
        message,
        type: 'error',
        onConfirm: () => {
          resolve();
        }
      });
    });
  }

  warning(message: string, title?: string): Promise<void> {
    return new Promise((resolve) => {
      this.show({
        title: title || 'Warning',
        message,
        type: 'warning',
        onConfirm: () => {
          resolve();
        }
      });
    });
  }

  confirm(message: string, title?: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.show({
        title: title || 'Confirm',
        message,
        type: 'confirm',
        confirmText: 'OK',
        cancelText: 'Cancel',
        onConfirm: () => {
          resolve(true);
        },
        onCancel: () => {
          resolve(false);
        }
      });
    });
  }
}

