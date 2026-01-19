import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserRole } from '../../models/user.model';
import { filter } from 'rxjs/operators';

interface NavItem {
  label: string;
  route: string;
  icon: string;
  roles: UserRole[];
}

@Component({
  selector: 'app-shell',
  templateUrl: './app-shell.component.html',
  styleUrls: ['./app-shell.component.scss']
})
export class AppShellComponent implements OnInit {
  readonly UserRole = UserRole;
  
  currentRoute = '';
  sidebarExpanded = false;

  navItems: NavItem[] = [
    {
      label: 'Billing',
      route: '/billing',
      icon: 'billing',
      roles: [UserRole.CASHIER, UserRole.ADMIN]
    },
    {
      label: 'Inventory',
      route: '/inventory',
      icon: 'inventory',
      roles: [UserRole.STOCK_MONITOR, UserRole.ADMIN]
    },
    {
      label: 'Medicines',
      route: '/medicines',
      icon: 'medicines',
      roles: [UserRole.STOCK_KEEPER, UserRole.ADMIN]
    },
    {
      label: 'Returns',
      route: '/returns',
      icon: 'returns',
      roles: [UserRole.CUSTOMER_SUPPORT, UserRole.ADMIN]
    },
    {
      label: 'Reports',
      route: '/reports',
      icon: 'reports',
      roles: [UserRole.ANALYST, UserRole.MANAGER, UserRole.ADMIN]
    },
    {
      label: 'Purchase History',
      route: '/purchase-history',
      icon: 'purchase-history',
      roles: [UserRole.MANAGER, UserRole.ADMIN]
    },
    {
      label: 'User Activity',
      route: '/user-activity',
      icon: 'user-activity',
      roles: [UserRole.ADMIN]
    },
    {
      label: 'Login History',
      route: '/login-history',
      icon: 'login-history',
      roles: [UserRole.ADMIN]
    },
    {
      label: 'User Management',
      route: '/user-management',
      icon: 'user-management',
      roles: [UserRole.ADMIN]
    }
  ];

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Track current route for active state
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.currentRoute = event.url;
      });
    
    this.currentRoute = this.router.url;
  }

  get visibleNavItems(): NavItem[] {
    return this.navItems.filter(item => 
      this.authService.hasAnyRole(item.roles)
    );
  }

  isActive(route: string): boolean {
    return this.currentRoute.startsWith(route);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}

