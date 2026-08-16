import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import type { Observable} from 'rxjs';
import { tap } from 'rxjs';

import { environment } from '../../environments/environment';
import type { Game } from '../models/game.model';

@Injectable({ providedIn: 'root' })
export class FavoriteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/favorites`;

  private readonly favoriteIdsSignal = signal<Set<number>>(new Set());
  readonly favoriteIds = this.favoriteIdsSignal.asReadonly();

  isFavorite(gameId: number): boolean {
    return this.favoriteIdsSignal().has(gameId);
  }

  /** (Re)charge la liste des favoris depuis le serveur : à appeler après connexion. */
  refresh(): void {
    this.http.get<Game[]>(this.baseUrl).subscribe({
      next: (games) => { this.favoriteIdsSignal.set(new Set(games.map((g) => g.id))); },
      error: () => { this.favoriteIdsSignal.set(new Set()); }
    });
  }

  /** À appeler à la déconnexion, pour ne pas garder les favoris d'un autre compte. */
  clear(): void {
    this.favoriteIdsSignal.set(new Set());
  }

  getFavorites(): Observable<Game[]> {
    return this.http.get<Game[]>(this.baseUrl);
  }

  toggle(gameId: number): void {
    if (this.isFavorite(gameId)) {
      this.remove(gameId).subscribe();
    } else {
      this.add(gameId).subscribe();
    }
  }

  private add(gameId: number): Observable<void> {
    // Seule façon de typer une réponse HTTP sans corps avec HttpClient.
    // eslint-disable-next-line @typescript-eslint/no-invalid-void-type
    return this.http.post<void>(`${this.baseUrl}/${gameId}`, {}).pipe(
      tap(() => { this.favoriteIdsSignal.update((ids) => new Set(ids).add(gameId)); })
    );
  }

  private remove(gameId: number): Observable<void> {
    // Seule façon de typer une réponse HTTP sans corps avec HttpClient.
    // eslint-disable-next-line @typescript-eslint/no-invalid-void-type
    return this.http.delete<void>(`${this.baseUrl}/${gameId}`).pipe(
      tap(() =>
        { this.favoriteIdsSignal.update((ids) => {
          const next = new Set(ids);
          next.delete(gameId);
          return next;
        }); }
      )
    );
  }
}
