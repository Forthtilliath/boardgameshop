import { HttpClient } from '@angular/common/http';
import { inject,Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type { AdminGameRequest, Game } from '../models/game.model';

@Injectable({ providedIn: 'root' })
export class AdminGameService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/games`;

  getGames(): Observable<Game[]> {
    return this.http.get<Game[]>(this.baseUrl);
  }

  createGame(request: AdminGameRequest): Observable<Game> {
    return this.http.post<Game>(this.baseUrl, request);
  }

  updateGame(id: number, request: AdminGameRequest): Observable<Game> {
    return this.http.put<Game>(`${this.baseUrl}/${id}`, request);
  }

  deleteGame(id: number): Observable<void> {
    // Seule facon de typer une reponse HTTP sans corps avec HttpClient.
    // eslint-disable-next-line @typescript-eslint/no-invalid-void-type
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
