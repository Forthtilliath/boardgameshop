import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import type { Game } from '../../../models/game.model';
import { AdminGameService } from '../../../services/admin-game.service';

@Component({
  selector: 'app-admin-games-list',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './admin-games-list.component.html',
  styleUrl: './admin-games-list.component.scss'
})
export class AdminGamesListComponent {
  private readonly adminGameService = inject(AdminGameService);

  readonly games = signal<Game[]>([]);
  readonly loading = signal(true);

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
    if (!confirm(`Supprimer "${game.name}" ? Cette action est irreversible.`)) {
      return;
    }
    this.adminGameService.deleteGame(game.id).subscribe(() => { this.loadGames(); });
  }
}
