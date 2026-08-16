import { Component, inject, signal } from '@angular/core';

import { GameCardComponent } from '../../components/game-card/game-card.component';
import type { Game } from '../../models/game.model';
import { CartService } from '../../services/cart.service';
import { FavoriteService } from '../../services/favorite.service';

@Component({
  selector: 'app-favorites',
  imports: [GameCardComponent],
  templateUrl: './favorites.component.html',
  styleUrl: './favorites.component.scss'
})
export class FavoritesComponent {
  private readonly favoriteService = inject(FavoriteService);
  private readonly cartService = inject(CartService);

  readonly games = signal<Game[]>([]);
  readonly loading = signal(true);

  constructor() {
    this.favoriteService.getFavorites().subscribe((games) => {
      this.games.set(games);
      this.loading.set(false);
    });
  }

  addToCart(game: Game): void {
    this.cartService.addToCart(game);
  }
}
