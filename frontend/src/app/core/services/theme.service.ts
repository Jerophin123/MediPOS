import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type ThemeMode = 'light' | 'dark' | 'system';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private renderer: Renderer2;
  private readonly THEME_STORAGE_KEY = 'theme_preference';
  private themeSubject = new BehaviorSubject<ThemeMode>('system');
  public theme$: Observable<ThemeMode> = this.themeSubject.asObservable();

  constructor(rendererFactory: RendererFactory2) {
    this.renderer = rendererFactory.createRenderer(null, null);
    // Apply theme immediately if DOM is available
    if (typeof document !== 'undefined') {
      this.initializeTheme();
    }
  }

  private initializeTheme(): void {
    // Get saved preference or default to 'system'
    const savedTheme = (localStorage.getItem(this.THEME_STORAGE_KEY) as ThemeMode) || 'system';
    this.themeSubject.next(savedTheme);
    // Apply theme immediately
    this.applyTheme();
  }

  /**
   * Get current effective theme (resolves 'system' to actual light/dark)
   */
  getEffectiveTheme(): 'light' | 'dark' {
    const theme = this.themeSubject.value;
    if (theme === 'system') {
      return this.getSystemTheme();
    }
    return theme;
  }

  /**
   * Get system theme preference
   */
  private getSystemTheme(): 'light' | 'dark' {
    if (typeof window !== 'undefined' && window.matchMedia) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    return 'light';
  }

  /**
   * Set theme mode
   */
  setTheme(theme: ThemeMode): void {
    this.themeSubject.next(theme);
    localStorage.setItem(this.THEME_STORAGE_KEY, theme);
    this.applyTheme();
  }

  /**
   * Toggle between light and dark (skips system mode)
   */
  toggleTheme(): void {
    const current = this.getEffectiveTheme();
    this.setTheme(current === 'light' ? 'dark' : 'light');
  }

  /**
   * Apply theme to document
   */
  private applyTheme(): void {
    if (typeof document === 'undefined') {
      return;
    }
    
    const effectiveTheme = this.getEffectiveTheme();
    const htmlElement = document.documentElement;

    // Remove both classes first to avoid conflicts
    this.renderer.removeClass(htmlElement, 'dark-theme');
    this.renderer.removeClass(htmlElement, 'light-theme');
    
    // Add the appropriate class
    if (effectiveTheme === 'dark') {
      this.renderer.addClass(htmlElement, 'dark-theme');
    } else {
      this.renderer.addClass(htmlElement, 'light-theme');
    }
  }

  /**
   * Listen to system theme changes
   */
  watchSystemTheme(): void {
    if (typeof window !== 'undefined' && window.matchMedia) {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
      
      // Use addEventListener for better browser support
      if (mediaQuery.addEventListener) {
        mediaQuery.addEventListener('change', () => {
          if (this.themeSubject.value === 'system') {
            this.applyTheme();
          }
        });
      } else {
        // Fallback for older browsers
        mediaQuery.addListener(() => {
          if (this.themeSubject.value === 'system') {
            this.applyTheme();
          }
        });
      }
    }
  }
}

