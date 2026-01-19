import { Component, OnInit } from '@angular/core';
import { AuditLogService, AuditLogResponse } from '../../core/services/audit-log.service';

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

  constructor(private auditLogService: AuditLogService) {}

  ngOnInit(): void {
    this.loadLoginLogoutLogs();
  }

  loadLoginLogoutLogs(): void {
    this.isLoading = true;
    this.auditLogService.getLoginLogoutLogs().subscribe({
      next: (logs) => {
        this.loginLogoutLogs = logs;
        this.filteredLogs = logs;
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
}

