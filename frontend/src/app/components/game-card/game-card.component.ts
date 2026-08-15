import { CurrencyPipe } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Game } from '../../models/game.model';

@Component({
  selector: 'app-game-card',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './game-card.component.html',
  styleUrl: './game-card.component.scss'
})
export class GameCardComponent {
  @Input({ required: true }) game!: Game;
  @Output() addToCart = new EventEmitter<Game>();

  onAddToCart(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.addToCart.emit(this.game);
  }
}
