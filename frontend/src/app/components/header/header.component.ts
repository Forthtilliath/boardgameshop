import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { StockAlertService } from '../../services/stock-alert.service';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  private readonly cartService = inject(CartService);
  private readonly authService = inject(AuthService);
  private readonly stockAlertService = inject(StockAlertService);
  private readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);

  readonly theme = this.themeService.theme;

  toggleTheme(): void {
    this.themeService.toggle();
  }

  readonly itemCount = this.cartService.itemCount;
  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly isAdmin = this.authService.isAdmin;

  readonly readyAlerts = this.stockAlertService.readyAlerts;
  readonly readyAlertsMenuOpen = signal(false);

  toggleReadyAlertsMenu(): void {
    this.readyAlertsMenuOpen.update((open) => !open);
  }

  closeReadyAlertsMenu(): void {
    this.readyAlertsMenuOpen.set(false);
  }

  dismissReadyAlert(gameId: number): void {
    this.stockAlertService.dismissReadyAlert(gameId).subscribe();
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/');
  }
}
