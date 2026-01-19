import { Component, OnInit } from '@angular/core';
import { AuditLogService, AuditLogResponse } from '../../core/services/audit-log.service';

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

  constructor(private auditLogService: AuditLogService) {}

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  loadAuditLogs(): void {
    this.isLoading = true;
    this.auditLogService.getAllAuditLogs().subscribe({
      next: (logs) => {
        this.auditLogs = logs;
        this.filteredLogs = logs;
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
}

