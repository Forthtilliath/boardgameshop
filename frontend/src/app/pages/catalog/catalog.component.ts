import type { ElementRef } from '@angular/core';
import { Component, computed, effect, inject, signal, viewChild } from '@angular/core';

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

/** Nombre de jeux affiches initialement, puis ajoutes a chaque atteinte du bas de page. */
const PAGE_SIZE = 24;

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

  /** Pagination cote client : le back renvoie la liste filtree complete, on n'en affiche qu'une tranche. */
  private readonly visibleCount = signal(PAGE_SIZE);
  readonly visibleGames = computed(() => this.games().slice(0, this.visibleCount()));
  readonly hasMore = computed(() => this.visibleCount() < this.games().length);

  private readonly sentinel = viewChild<ElementRef<HTMLDivElement>>('sentinel');
  private observer: IntersectionObserver | null = null;

  constructor() {
    this.tagService.getPublicTags().subscribe((tags) => { this.tags.set(tags); });

    // Chips de catégorie dérivées du catalogue complet (indépendant des filtres actifs).
    this.gameService.getGames().subscribe((games) => {
      this.categories.set(['Toutes', ...new Set(games.map((g) => g.category))]);
    });

    this.loadGames();

    // Reobserve le repere de fin de liste a chaque fois qu'il (re)apparait dans le DOM
    // (nouveau chargement, changement de filtres, ou epuisement de la page courante).
    effect(() => {
      const element = this.sentinel()?.nativeElement;
      this.observer?.disconnect();
      if (!element) {
        return;
      }
      this.observer = new IntersectionObserver((entries) => {
        if (entries[0]?.isIntersecting) {
          this.showMore();
        }
      });
      this.observer.observe(element);
    });
  }

  showMore(): void {
    this.visibleCount.update((count) => Math.min(count + PAGE_SIZE, this.games().length));
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
        this.visibleCount.set(PAGE_SIZE);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }
}
