import { Component, computed, inject, signal } from '@angular/core';

import { GameCardComponent } from '../../components/game-card/game-card.component';
import { Game } from '../../models/game.model';
import { CartService } from '../../services/cart.service';
import { GameService } from '../../services/game.service';

@Component({
  selector: 'app-catalog',
  imports: [GameCardComponent],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.scss'
})
export class CatalogComponent {
  private readonly gameService = inject(GameService);
  private readonly cartService = inject(CartService);

  private readonly games = signal<Game[]>([]);
  private readonly selectedCategory = signal<string>('Toutes');
  readonly loading = signal(true);
  readonly error = signal(false);

  readonly categories = computed(() => ['Toutes', ...new Set(this.games().map((g) => g.category))]);
  readonly selected = this.selectedCategory.asReadonly();

  readonly filteredGames = computed(() => {
    const category = this.selectedCategory();
    return category === 'Toutes'
      ? this.games()
      : this.games().filter((g) => g.category === category);
  });

  constructor() {
    this.gameService.getGames().subscribe({
      next: (games) => {
        this.games.set(games);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  selectCategory(category: string): void {
    this.selectedCategory.set(category);
  }

  addToCart(game: Game): void {
    this.cartService.addToCart(game);
  }
}
