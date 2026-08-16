import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { Game } from '../../models/game.model';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { FavoriteService } from '../../services/favorite.service';
import { GameService } from '../../services/game.service';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-game-detail',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './game-detail.component.html',
  styleUrl: './game-detail.component.scss'
})
export class GameDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly gameService = inject(GameService);
  private readonly cartService = inject(CartService);
  private readonly favoriteService = inject(FavoriteService);
  private readonly authService = inject(AuthService);

  readonly game = signal<Game | null>(null);
  readonly notFound = signal(false);
  readonly quantity = signal(1);
  readonly added = signal(false);
  readonly isAuthenticated = this.authService.isAuthenticated;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.gameService.getGame(id).subscribe({
      next: (game) => this.game.set(game),
      error: () => this.notFound.set(true)
    });
  }

  decrement(): void {
    this.quantity.update((q) => Math.max(1, q - 1));
  }

  increment(): void {
    const game = this.game();
    this.quantity.update((q) => (game ? Math.min(game.stock, q + 1) : q + 1));
  }

  addToCart(): void {
    const game = this.game();
    if (!game) {
      return;
    }
    this.cartService.addToCart(game, this.quantity());
    this.added.set(true);
    setTimeout(() => this.added.set(false), 2000);
  }

  isFavorite(): boolean {
    const game = this.game();
    return game !== null && this.favoriteService.isFavorite(game.id);
  }

  toggleFavorite(): void {
    const game = this.game();
    if (game) {
      this.favoriteService.toggle(game.id);
    }
  }
}
