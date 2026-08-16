import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { GameCardComponent } from '../../components/game-card/game-card.component';
import type { Game } from '../../models/game.model';
import type { Home } from '../../models/home.model';
import { CartService } from '../../services/cart.service';
import { HomeService } from '../../services/home.service';

@Component({
  selector: 'app-home',
  imports: [GameCardComponent, RouterLink, DatePipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  private readonly homeService = inject(HomeService);
  private readonly cartService = inject(CartService);

  readonly home = signal<Home | null>(null);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly ratingScale = [1, 2, 3, 4, 5];

  constructor() {
    this.homeService.getHome().subscribe({
      next: (home) => {
        this.home.set(home);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  addToCart(game: Game): void {
    this.cartService.addToCart(game);
  }
}
