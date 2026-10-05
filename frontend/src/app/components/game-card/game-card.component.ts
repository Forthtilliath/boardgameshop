import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

import type { Game } from '../../models/game.model';
import { AuthService } from '../../services/auth.service';
import { FavoriteService } from '../../services/favorite.service';
import { pionColor } from '../../utils/pion-color';

@Component({
  selector: 'app-game-card',
  imports: [RouterLink, CurrencyPipe, DecimalPipe],
  templateUrl: './game-card.component.html',
  styleUrl: './game-card.component.scss',
  host: { '[style.--card-color]': 'cardColor()' }
})
export class GameCardComponent {
  private readonly favoriteService = inject(FavoriteService);
  private readonly authService = inject(AuthService);

  readonly game = input.required<Game>();
  readonly addToCart = output<Game>();

  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly cardColor = computed(() => pionColor(this.game().id));

  isFavorite(): boolean {
    return this.favoriteService.isFavorite(this.game().id);
  }

  onAddToCart(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.addToCart.emit(this.game());
  }

  onToggleFavorite(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.favoriteService.toggle(this.game().id);
  }
}
