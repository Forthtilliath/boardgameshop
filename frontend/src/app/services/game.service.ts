import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Game } from '../models/game.model';

@Injectable({ providedIn: 'root' })
export class GameService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/games`;

  getGames(category?: string): Observable<Game[]> {
    if (category) {
      return this.http.get<Game[]>(this.baseUrl, { params: { category } });
    }
    return this.http.get<Game[]>(this.baseUrl);
  }

  getGame(id: number): Observable<Game> {
    return this.http.get<Game>(`${this.baseUrl}/${id}`);
  }
}
