import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../auth.service';
import { UserRole } from '../../core/models/user.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage = '';
  isLoading = false;
  returnUrl = '';
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || this.getDefaultRoute();
    
    if (this.authService.isAuthenticated()) {
      const user = this.authService.getCurrentUser();
      const defaultRoute = user ? this.getDefaultRouteForRole(user.role) : '/billing';
      this.router.navigate([this.returnUrl || defaultRoute]);
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        const user = this.authService.getCurrentUser();
        const defaultRoute = user ? this.getDefaultRouteForRole(user.role) : '/billing';
        // Use returnUrl if provided, otherwise use role-based default
        const targetRoute = this.returnUrl && this.returnUrl !== '/billing' 
          ? this.returnUrl 
          : defaultRoute;
        this.router.navigate([targetRoute]);
      },
      error: (error) => {
        this.errorMessage = error.message || 'Login failed. Please check your credentials.';
        this.isLoading = false;
      }
    });
  }

  private getDefaultRoute(): string {
    return '/billing'; // Fallback default
  }

  private getDefaultRouteForRole(role: UserRole | string): string {
    switch (role) {
      case UserRole.CASHIER:
        return '/billing';
      case UserRole.STOCK_MONITOR:
        return '/inventory';
      case UserRole.STOCK_KEEPER:
        return '/medicines';
      case UserRole.CUSTOMER_SUPPORT:
        return '/returns';
      case UserRole.ANALYST:
      case UserRole.MANAGER:
        return '/reports';
      case UserRole.ADMIN:
        return '/billing';
      default:
        return '/billing';
    }
  }

  get username() {
    return this.loginForm.get('username');
  }

  get password() {
    return this.loginForm.get('password');
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }
}



