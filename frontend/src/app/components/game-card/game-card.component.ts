import { CurrencyPipe } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Game } from '../../models/game.model';
import { AuthService } from '../../services/auth.service';
import { FavoriteService } from '../../services/favorite.service';

@Component({
  selector: 'app-game-card',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './game-card.component.html',
  styleUrl: './game-card.component.scss'
})
export class GameCardComponent {
  private readonly favoriteService = inject(FavoriteService);
  private readonly authService = inject(AuthService);

  @Input({ required: true }) game!: Game;
  @Output() addToCart = new EventEmitter<Game>();

  readonly isAuthenticated = this.authService.isAuthenticated;

  isFavorite(): boolean {
    return this.favoriteService.isFavorite(this.game.id);
  }

  onAddToCart(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.addToCart.emit(this.game);
  }

  onToggleFavorite(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.favoriteService.toggle(this.game.id);
  }
}
