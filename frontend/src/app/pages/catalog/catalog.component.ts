import type { ElementRef } from '@angular/core';
import { Component, effect, inject, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import type { ParamMap } from '@angular/router';
import { ActivatedRoute, Router } from '@angular/router';

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
  { value: 'rating', label: 'Mieux notes' },
  { value: 'price_asc', label: 'Prix croissant' },
  { value: 'price_desc', label: 'Prix decroissant' }
];

/** Taille d'une page catalogue, cote back comme cote front. */
const PAGE_SIZE = 24;
const SEARCH_DEBOUNCE_MS = 300;

interface ParsedFilters {
  category: string;
  sort: GameSort | '';
  search: string;
  priceMin: number | null;
  priceMax: number | null;
  players: number | null;
  maxDuration: number | null;
  age: number | null;
  tagIds: Set<number>;
}

function parseIntOrNull(value: string | null): number | null {
  if (!value) return null;
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

function parseFilters(params: ParamMap): ParsedFilters {
  return {
    category: params.get('category') ?? 'Toutes',
    sort: (params.get('sort') as GameSort | null) ?? '',
    search: params.get('q') ?? '',
    priceMin: parseIntOrNull(params.get('priceMin')),
    priceMax: parseIntOrNull(params.get('priceMax')),
    players: parseIntOrNull(params.get('players')),
    maxDuration: parseIntOrNull(params.get('maxDuration')),
    age: parseIntOrNull(params.get('age')),
    tagIds: new Set(params.getAll('tags').map(Number).filter(Number.isFinite))
  };
}

/**
 * L'état des filtres vit dans l'URL (query params) : partageable, restauré au
 * rechargement, et le bouton retour du navigateur fonctionne naturellement.
 * Toute modification de filtre passe par `router.navigate(...)`, jamais par un
 * appel direct à `loadGames()` — c'est l'effet ci-dessous (déclenché par le
 * changement de `queryParamMap`) qui est l'unique point d'entrée du chargement,
 * même pattern que `GameDetailComponent` pour la réactivité aux routes.
 */
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
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly sortOptions = SORT_OPTIONS;

  private readonly queryParams = toSignal(this.route.queryParamMap, { requireSync: true });

  readonly games = signal<Game[]>([]);
  readonly totalElements = signal(0);
  readonly categories = signal<string[]>(['Toutes']);
  readonly tags = signal<Tag[]>([]);
  readonly loading = signal(true);
  readonly loadingMore = signal(false);
  readonly error = signal(false);

  readonly searchInput = signal('');
  readonly priceMinInput = signal<number | null>(null);
  readonly priceMaxInput = signal<number | null>(null);
  readonly playersInput = signal<number | null>(null);
  readonly maxDurationInput = signal<number | null>(null);
  readonly ageInput = signal<number | null>(null);
  readonly selectedTagIds = signal<Set<number>>(new Set());
  readonly selectedCategory = signal('Toutes');
  readonly selectedSort = signal<GameSort | ''>('');

  readonly hasMore = signal(false);

  private page = 0;
  private totalPages = 1;
  private searchDebounceTimer: ReturnType<typeof setTimeout> | null = null;

  private readonly sentinel = viewChild<ElementRef<HTMLDivElement>>('sentinel');
  private observer: IntersectionObserver | null = null;

  constructor() {
    this.tagService.getPublicTags().subscribe((tags) => { this.tags.set(tags); });
    this.gameService.getCategories().subscribe((categories) => {
      this.categories.set(['Toutes', ...categories]);
    });

    effect(() => {
      const parsed = parseFilters(this.queryParams());

      this.selectedCategory.set(parsed.category);
      this.selectedSort.set(parsed.sort);
      this.searchInput.set(parsed.search);
      this.priceMinInput.set(parsed.priceMin);
      this.priceMaxInput.set(parsed.priceMax);
      this.playersInput.set(parsed.players);
      this.maxDurationInput.set(parsed.maxDuration);
      this.ageInput.set(parsed.age);
      this.selectedTagIds.set(parsed.tagIds);

      this.loadPage(0, parsed);
    });

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
          this.loadMore();
        }
      });
      this.observer.observe(element);
    });
  }

  selectCategory(category: string): void {
    this.navigate({ category: category === 'Toutes' ? null : category });
  }

  onSortChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.navigate({ sort: value || null });
  }

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchInput.set(value);
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
    }
    this.searchDebounceTimer = setTimeout(() => {
      this.navigate({ q: value || null });
    }, SEARCH_DEBOUNCE_MS);
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
    this.navigate({
      priceMin: this.priceMinInput() != null ? String(this.priceMinInput()) : null,
      priceMax: this.priceMaxInput() != null ? String(this.priceMaxInput()) : null,
      players: this.playersInput() != null ? String(this.playersInput()) : null,
      maxDuration: this.maxDurationInput() != null ? String(this.maxDurationInput()) : null,
      age: this.ageInput() != null ? String(this.ageInput()) : null,
      tags: this.selectedTagIds().size > 0 ? [...this.selectedTagIds()].map(String) : null
    });
  }

  resetFilters(): void {
    void this.router.navigate(['/jeux']);
  }

  addToCart(game: Game): void {
    this.cartService.addToCart(game);
  }

  private navigate(patch: Record<string, string | string[] | null>): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: patch,
      queryParamsHandling: 'merge'
    });
  }

  private loadMore(): void {
    if (!this.hasMore() || this.loadingMore()) {
      return;
    }
    this.loadPage(this.page + 1, parseFilters(this.queryParams()), true);
  }

  private loadPage(page: number, parsed: ParsedFilters, append = false): void {
    if (append) {
      this.loadingMore.set(true);
    } else {
      this.loading.set(true);
    }
    this.error.set(false);

    const filter: GameFilter = {
      category: parsed.category === 'Toutes' ? undefined : parsed.category,
      priceMin: parsed.priceMin ?? undefined,
      priceMax: parsed.priceMax ?? undefined,
      players: parsed.players ?? undefined,
      maxDuration: parsed.maxDuration ?? undefined,
      age: parsed.age ?? undefined,
      tags: [...parsed.tagIds],
      sort: parsed.sort || undefined,
      search: parsed.search || undefined
    };

    this.gameService.getGames(filter, page, PAGE_SIZE).subscribe({
      next: (result) => {
        this.games.set(append ? [...this.games(), ...result.content] : result.content);
        this.totalElements.set(result.totalElements);
        this.page = result.page;
        this.totalPages = result.totalPages;
        this.hasMore.set(this.page + 1 < this.totalPages);
        this.loading.set(false);
        this.loadingMore.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
        this.loadingMore.set(false);
      }
    });
  }
}
