import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { GameCardComponent } from '../../components/game-card/game-card.component';
import type { Game } from '../../models/game.model';
import { CartService } from '../../services/cart.service';
import { FavoriteService } from '../../services/favorite.service';

/** Consultation publique (sans compte) de la liste de favoris partagée par un autre utilisateur. */
@Component({
  selector: 'app-shared-favorites',
  imports: [GameCardComponent, RouterLink],
  templateUrl: './shared-favorites.component.html',
  styleUrl: './shared-favorites.component.scss'
})
export class SharedFavoritesComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly favoriteService = inject(FavoriteService);
  private readonly cartService = inject(CartService);

  readonly games = signal<Game[]>([]);
  readonly loading = signal(true);
  readonly notFound = signal(false);

  constructor() {
    const token = this.route.snapshot.paramMap.get('token');
    if (!token) {
      this.notFound.set(true);
      this.loading.set(false);
      return;
    }

    this.favoriteService.getSharedFavorites(token).subscribe({
      next: (games) => {
        this.games.set(games);
        this.loading.set(false);
      },
      error: () => {
        this.notFound.set(true);
        this.loading.set(false);
      }
    });
  }

  addToCart(game: Game): void {
    this.cartService.addToCart(game);
  }
}
