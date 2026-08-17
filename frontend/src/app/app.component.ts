import { Component, effect, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { FooterComponent } from './components/footer/footer.component';
import { HeaderComponent } from './components/header/header.component';
import { AuthService } from './services/auth.service';
import { FavoriteService } from './services/favorite.service';
import { StockAlertService } from './services/stock-alert.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private readonly authService = inject(AuthService);
  private readonly favoriteService = inject(FavoriteService);
  private readonly stockAlertService = inject(StockAlertService);

  constructor() {
    // Synchronise favoris et alertes stock avec l'état de connexion
    // (connexion/déconnexion/restauration de session).
    effect(() => {
      if (this.authService.isAuthenticated()) {
        this.favoriteService.refresh();
        this.stockAlertService.refreshReadyAlerts();
      } else {
        this.favoriteService.clear();
        this.stockAlertService.clear();
      }
    });
  }
}
