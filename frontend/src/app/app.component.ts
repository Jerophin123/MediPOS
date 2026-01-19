import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth/auth.service';
import { UserRole } from './core/models/user.model';
import { ThemeService } from './core/services/theme.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'Medical Store POS';
  isAuthenticated = false;
  readonly UserRole = UserRole;

  constructor(
    public authService: AuthService,
    private router: Router,
    private themeService: ThemeService
  ) {}

  get currentUser$() {
    return this.authService.currentUser$;
  }

  ngOnInit(): void {
    // Initialize theme service (detects system preference)
    this.themeService.watchSystemTheme();
    
    this.authService.currentUser$.subscribe(user => {
      this.isAuthenticated = !!user;
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}

