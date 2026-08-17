import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import type { Observable } from 'rxjs';
import { tap } from 'rxjs';

import { environment } from '../../environments/environment';
import type { Game } from '../models/game.model';

/** Alertes "retour en stock" (in-app uniquement, pas d'email). */
@Injectable({ providedIn: 'root' })
export class StockAlertService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/stock-alerts`;

  /** Jeux suivis désormais de nouveau en stock : rafraîchi après connexion, affiché en badge dans le header. */
  private readonly readyAlertsSignal = signal<Game[]>([]);
  readonly readyAlerts = this.readyAlertsSignal.asReadonly();

  refreshReadyAlerts(): void {
    this.http.get<Game[]>(`${this.baseUrl}/ready`).subscribe({
      next: (games) => { this.readyAlertsSignal.set(games); },
      error: () => { this.readyAlertsSignal.set([]); }
    });
  }

  clear(): void {
    this.readyAlertsSignal.set([]);
  }

  isSubscribed(gameId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/${gameId}`);
  }

  subscribe(gameId: number): Observable<void> {
    // Seule façon de typer une réponse HTTP sans corps avec HttpClient.
    // eslint-disable-next-line @typescript-eslint/no-invalid-void-type
    return this.http.post<void>(`${this.baseUrl}/${gameId}`, {});
  }

  unsubscribe(gameId: number): Observable<void> {
    // eslint-disable-next-line @typescript-eslint/no-invalid-void-type
    return this.http.delete<void>(`${this.baseUrl}/${gameId}`);
  }

  dismissReadyAlert(gameId: number): Observable<void> {
    // eslint-disable-next-line @typescript-eslint/no-invalid-void-type
    return this.http.delete<void>(`${this.baseUrl}/ready/${gameId}`).pipe(
      tap(() => {
        this.readyAlertsSignal.update((games) => games.filter((g) => g.id !== gameId));
      })
    );
  }
}
