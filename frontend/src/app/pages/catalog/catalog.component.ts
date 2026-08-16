import { Component, inject, signal } from '@angular/core';

import { GameCardComponent } from '../../components/game-card/game-card.component';
import type { Game } from '../../models/game.model';
import type { GameFilter, GameSort } from '../../models/game-filter.model';
import type { Tag } from '../../models/tag.model';
import { CartService } from '../../services/cart.service';
import { GameService } from '../../services/game.service';
import { TagService } from '../../services/tag.service';

const SORT_OPTIONS: { value: GameSort | ''; label: string }[] = [
  { value: '', label: 'Pertinence' },
  { value: 'newest', label: 'Nouveautes' },
  { value: 'popularity', label: 'Meilleures ventes' },
  { value: 'price_asc', label: 'Prix croissant' },
  { value: 'price_desc', label: 'Prix decroissant' }
];

@Component({
  selector: 'app-catalog',
  imports: [GameCardComponent],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.scss'
})
export class CatalogComponent {
  private readonly gameService = inject(GameService);
  private readonly tagService = inject(TagService);
  private readonly cartService = inject(CartService);

  readonly sortOptions = SORT_OPTIONS;

  readonly games = signal<Game[]>([]);
  readonly categories = signal<string[]>(['Toutes']);
  readonly tags = signal<Tag[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);

  readonly selectedCategory = signal('Toutes');
  readonly selectedSort = signal<GameSort | ''>('');
  readonly priceMin = signal<number | null>(null);
  readonly priceMax = signal<number | null>(null);
  readonly players = signal<number | null>(null);
  readonly maxDuration = signal<number | null>(null);
  readonly age = signal<number | null>(null);
  readonly selectedTagIds = signal<Set<number>>(new Set());

  constructor() {
    this.tagService.getPublicTags().subscribe((tags) => { this.tags.set(tags); });

    // Chips de catégorie dérivées du catalogue complet (indépendant des filtres actifs).
    this.gameService.getGames().subscribe((games) => {
      this.categories.set(['Toutes', ...new Set(games.map((g) => g.category))]);
    });

    this.loadGames();
  }

  selectCategory(category: string): void {
    this.selectedCategory.set(category);
    this.loadGames();
  }

  onSortChange(event: Event): void {
    this.selectedSort.set((event.target as HTMLSelectElement).value as GameSort | '');
    this.loadGames();
  }

  toggleTag(tagId: number): void {
    const current = new Set(this.selectedTagIds());
    if (current.has(tagId)) {
      current.delete(tagId);
    } else {
      current.add(tagId);
    }
    this.selectedTagIds.set(current);
  }

  applyFilters(): void {
    this.loadGames();
  }

  resetFilters(): void {
    this.selectedCategory.set('Toutes');
    this.selectedSort.set('');
    this.priceMin.set(null);
    this.priceMax.set(null);
    this.players.set(null);
    this.maxDuration.set(null);
    this.age.set(null);
    this.selectedTagIds.set(new Set());
    this.loadGames();
  }

  addToCart(game: Game): void {
    this.cartService.addToCart(game);
  }

  private loadGames(): void {
    this.loading.set(true);
    this.error.set(false);

    const filter: GameFilter = {
      category: this.selectedCategory() === 'Toutes' ? undefined : this.selectedCategory(),
      priceMin: this.priceMin() ?? undefined,
      priceMax: this.priceMax() ?? undefined,
      players: this.players() ?? undefined,
      maxDuration: this.maxDuration() ?? undefined,
      age: this.age() ?? undefined,
      tags: [...this.selectedTagIds()],
      sort: this.selectedSort() || undefined
    };

    this.gameService.getGames(filter).subscribe({
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
}
