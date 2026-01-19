import { Component, OnInit } from '@angular/core';
import { AuditLogService, AuditLogResponse } from '../../core/services/audit-log.service';
import { DialogService } from '../../core/services/dialog.service';

@Component({
  selector: 'app-login-history',
  templateUrl: './login-history.component.html',
  styleUrls: ['./login-history.component.scss']
})
export class LoginHistoryComponent implements OnInit {
  loginLogoutLogs: AuditLogResponse[] = [];
  filteredLogs: AuditLogResponse[] = [];
  isLoading = false;
  searchTerm = '';
  selectedType: string = 'ALL'; // ALL, LOGIN, LOGOUT

  constructor(
    private auditLogService: AuditLogService,
    private dialogService: DialogService
  ) {}

  ngOnInit(): void {
    this.loadLoginLogoutLogs();
  }

  loadLoginLogoutLogs(): void {
    this.isLoading = true;
    this.auditLogService.getLoginLogoutLogs().subscribe({
      next: (logs) => {
        // Limit to top 50 activities (backend should already limit, but adding safety check)
        this.loginLogoutLogs = logs.slice(0, 50);
        this.filteredLogs = this.loginLogoutLogs;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading login/logout logs:', error);
        this.isLoading = false;
      }
    });
  }

  filterLogs(): void {
    this.filteredLogs = this.loginLogoutLogs.filter(log => {
      const matchesSearch = !this.searchTerm || 
        log.username.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        log.fullName.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      const matchesType = this.selectedType === 'ALL' || 
        (this.selectedType === 'LOGIN' && log.action === 'USER_LOGIN') ||
        (this.selectedType === 'LOGOUT' && log.action === 'USER_LOGOUT');
      
      return matchesSearch && matchesType;
    });
  }

  getActionType(action: string): string {
    return action === 'USER_LOGIN' ? 'LOGIN' : 'LOGOUT';
  }

  getActionBadgeClass(action: string): string {
    return action === 'USER_LOGIN' ? 'badge-login' : 'badge-logout';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  getTimeAgo(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  }

  clearLoginHistory(): void {
    this.dialogService.confirm(
      'Are you sure you want to delete all login/logout history? This action cannot be undone.',
      'Clear Login/Logout History'
    ).then((confirmed) => {
      if (confirmed) {
        this.isLoading = true;
        this.auditLogService.deleteLoginLogoutLogs().subscribe({
          next: () => {
            this.loginLogoutLogs = [];
            this.filteredLogs = [];
            this.isLoading = false;
            this.dialogService.success('Login/logout history has been cleared successfully.');
          },
          error: (error) => {
            console.error('Error clearing login history:', error);
            this.dialogService.error('Failed to clear login history. Please try again.');
            this.isLoading = false;
          }
        });
      }
    });
  }
}



