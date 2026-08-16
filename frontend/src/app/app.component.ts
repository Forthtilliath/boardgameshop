import { Component, effect, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { HeaderComponent } from './components/header/header.component';
import { AuthService } from './services/auth.service';
import { FavoriteService } from './services/favorite.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private readonly authService = inject(AuthService);
  private readonly favoriteService = inject(FavoriteService);

  constructor() {
    // Synchronise les favoris avec l'état de connexion (connexion/déconnexion/restauration de session).
    effect(() => {
      if (this.authService.isAuthenticated()) {
        this.favoriteService.refresh();
      } else {
        this.favoriteService.clear();
      }
    });
  }
}
