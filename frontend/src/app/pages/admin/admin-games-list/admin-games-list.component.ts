import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import type { Game } from '../../../models/game.model';
import { AdminGameService } from '../../../services/admin-game.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-admin-games-list',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './admin-games-list.component.html',
  styleUrl: './admin-games-list.component.scss'
})
export class AdminGamesListComponent {
  private readonly adminGameService = inject(AdminGameService);
  private readonly toastService = inject(ToastService);

  readonly games = signal<Game[]>([]);
  readonly loading = signal(true);
  readonly searchTerm = signal('');

  /** Recherche cote client (nom/editeur) : la liste complete est deja chargee en memoire. */
  readonly filteredGames = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    if (!term) {
      return this.games();
    }
    return this.games().filter(
      (game) =>
        game.name.toLowerCase().includes(term) || (game.publisher?.toLowerCase().includes(term) ?? false)
    );
  });

  constructor() {
    this.loadGames();
  }

  private loadGames(): void {
    this.loading.set(true);
    this.adminGameService.getGames().subscribe((games) => {
      this.games.set(games);
      this.loading.set(false);
    });
  }

  deleteGame(game: Game): void {
    if (!confirm(`Supprimer "${game.name}" ? Cette action est irréversible.`)) {
      return;
    }
    this.adminGameService.deleteGame(game.id).subscribe(() => {
      this.loadGames();
      this.toastService.success(`"${game.name}" supprimé`);
    });
  }
}
