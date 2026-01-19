import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-unauthorized',
  templateUrl: './unauthorized.component.html',
  styleUrls: ['./unauthorized.component.scss']
})
export class UnauthorizedComponent {
  constructor(
    private router: Router,
    public authService: AuthService
  ) {}

  goHome(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      // Redirect to user's default page based on role
      const defaultRoute = this.getDefaultRoute(user.role);
      this.router.navigate([defaultRoute]);
    } else {
      this.router.navigate(['/auth/login']);
    }
  }

  private getDefaultRoute(role: string): string {
    switch (role) {
      case 'CASHIER':
        return '/billing';
      case 'STOCK_MONITOR':
        return '/inventory';
      case 'STOCK_KEEPER':
        return '/medicines';
      case 'CUSTOMER_SUPPORT':
        return '/returns';
      case 'ANALYST':
      case 'MANAGER':
        return '/reports';
      case 'ADMIN':
        return '/billing';
      default:
        return '/billing';
    }
  }
}

