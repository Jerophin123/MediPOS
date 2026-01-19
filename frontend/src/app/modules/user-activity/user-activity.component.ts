import { Component, OnInit } from '@angular/core';
import { AuditLogService, AuditLogResponse } from '../../core/services/audit-log.service';
import { DialogService } from '../../core/services/dialog.service';

@Component({
  selector: 'app-user-activity',
  templateUrl: './user-activity.component.html',
  styleUrls: ['./user-activity.component.scss']
})
export class UserActivityComponent implements OnInit {
  auditLogs: AuditLogResponse[] = [];
  filteredLogs: AuditLogResponse[] = [];
  isLoading = false;
  searchTerm = '';
  selectedAction: string = 'ALL';
  selectedUser: string = 'ALL';
  
  uniqueUsers: string[] = [];
  uniqueActions: string[] = [];

  constructor(
    private auditLogService: AuditLogService,
    private dialogService: DialogService
  ) {}

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  loadAuditLogs(): void {
    this.isLoading = true;
    this.auditLogService.getAllAuditLogs().subscribe({
      next: (logs) => {
        // Limit to top 50 activities (backend should already limit, but adding safety check)
        this.auditLogs = logs.slice(0, 50);
        this.filteredLogs = this.auditLogs;
        this.extractUniqueValues();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading audit logs:', error);
        this.isLoading = false;
      }
    });
  }

  extractUniqueValues(): void {
    this.uniqueUsers = [...new Set(this.auditLogs.map(log => log.username))].sort();
    this.uniqueActions = [...new Set(this.auditLogs.map(log => log.action))].sort();
  }

  filterLogs(): void {
    this.filteredLogs = this.auditLogs.filter(log => {
      const matchesSearch = !this.searchTerm || 
        log.username.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        log.fullName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        log.description.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        log.entityType.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      const matchesAction = this.selectedAction === 'ALL' || log.action === this.selectedAction;
      const matchesUser = this.selectedUser === 'ALL' || log.username === this.selectedUser;
      
      return matchesSearch && matchesAction && matchesUser;
    });
  }

  getActionBadgeClass(action: string): string {
    if (action.includes('LOGIN') || action.includes('LOGOUT')) {
      return 'badge-login';
    } else if (action.includes('CREATE') || action.includes('ADD')) {
      return 'badge-success';
    } else if (action.includes('UPDATE') || action.includes('ADJUST')) {
      return 'badge-warning';
    } else if (action.includes('DELETE') || action.includes('CANCEL')) {
      return 'badge-danger';
    }
    return 'badge-info';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  clearAllLogs(): void {
    this.dialogService.confirm(
      'Are you sure you want to delete all activity logs? This action cannot be undone.',
      'Clear All Activity Logs'
    ).then((confirmed) => {
      if (confirmed) {
        this.isLoading = true;
        this.auditLogService.deleteAllAuditLogs().subscribe({
          next: () => {
            this.auditLogs = [];
            this.filteredLogs = [];
            this.uniqueUsers = [];
            this.uniqueActions = [];
            this.isLoading = false;
            this.dialogService.success('All activity logs have been cleared successfully.');
          },
          error: (error) => {
            console.error('Error clearing audit logs:', error);
            this.dialogService.error('Failed to clear activity logs. Please try again.');
            this.isLoading = false;
          }
        });
      }
    });
  }
}



