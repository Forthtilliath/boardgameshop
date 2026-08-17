import { Component, inject, signal } from '@angular/core';

import { GameCardComponent } from '../../components/game-card/game-card.component';
import type { Game } from '../../models/game.model';
import { CartService } from '../../services/cart.service';
import { FavoriteService } from '../../services/favorite.service';

function csvField(value: string | number): string {
  const text = String(value);
  return /[",\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
}

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
  readonly shareLinkCopied = signal(false);
  readonly sharing = signal(false);

  constructor() {
    this.favoriteService.getFavorites().subscribe((games) => {
      this.games.set(games);
      this.loading.set(false);
    });
  }

  addToCart(game: Game): void {
    this.cartService.addToCart(game);
  }

  shareFavorites(): void {
    this.sharing.set(true);
    this.favoriteService.getShareLink().subscribe({
      next: async ({ token }) => {
        this.sharing.set(false);
        const url = `${location.origin}/favoris/partages/${token}`;
        try {
          await navigator.clipboard.writeText(url);
        } catch {
          // Presse-papiers indisponible (permissions navigateur) : on ignore,
          // le lien reste consultable via l'URL affichée par l'app le cas échéant.
        }
        this.shareLinkCopied.set(true);
        setTimeout(() => { this.shareLinkCopied.set(false); }, 2500);
      },
      error: () => { this.sharing.set(false); }
    });
  }

  exportCsv(): void {
    const header = ['Nom', 'Éditeur', 'Catégorie', 'Prix'];
    const rows = this.games().map((g) => [g.name, g.publisher, g.category, String(g.finalPrice)]);
    const csv = [header, ...rows].map((row) => row.map(csvField).join(',')).join('\n');

    // BOM UTF-8 en tete : sans lui, Excel affiche mal les caracteres accentues.
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'mes-favoris-bgs.csv';
    link.click();
    URL.revokeObjectURL(url);
  }
}
