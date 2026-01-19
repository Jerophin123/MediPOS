import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogService } from '../../services/dialog.service';
import { Subscription } from 'rxjs';
import { SvgIconComponent } from '../svg-icon/svg-icon.component';
import { Nl2brPipe } from '../../pipes/nl2br.pipe';

export interface DialogData {
  title?: string;
  message: string;
  type?: 'info' | 'success' | 'warning' | 'error' | 'confirm';
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}

@Component({
  selector: 'app-dialog',
  standalone: true,
  imports: [CommonModule, SvgIconComponent, Nl2brPipe],
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.scss']
})
export class DialogComponent implements OnInit, OnDestroy {
  show = false;
  dialogData: DialogData | null = null;
  private subscription?: Subscription;

  constructor(private dialogService: DialogService) {}

  ngOnInit(): void {
    this.subscription = this.dialogService.dialogState.subscribe(data => {
      if (data) {
        this.dialogData = data;
        this.show = true;
      } else {
        this.show = false;
        this.dialogData = null;
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  onConfirm(): void {
    if (this.dialogData?.onConfirm) {
      this.dialogData.onConfirm();
    }
    this.close();
  }

  onCancel(): void {
    if (this.dialogData?.onCancel) {
      this.dialogData.onCancel();
    }
    this.close();
  }

  close(): void {
    this.dialogService.close();
  }

  getIconName(): string {
    switch (this.dialogData?.type) {
      case 'success':
        return 'success';
      case 'warning':
        return 'warning';
      case 'error':
        return 'error';
      case 'confirm':
        return 'warning';
      default:
        return 'warning';
    }
  }
}

