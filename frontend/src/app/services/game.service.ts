import { HttpClient, HttpParams } from '@angular/common/http';
import { inject,Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type { Game } from '../models/game.model';
import type { GameFilter } from '../models/game-filter.model';

@Injectable({ providedIn: 'root' })
export class GameService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/games`;

  getGames(filter?: GameFilter): Observable<Game[]> {
    let params = new HttpParams();

    if (filter) {
      if (filter.category) params = params.set('category', filter.category);
      if (filter.priceMin != null) params = params.set('priceMin', filter.priceMin);
      if (filter.priceMax != null) params = params.set('priceMax', filter.priceMax);
      if (filter.players != null) params = params.set('players', filter.players);
      if (filter.maxDuration != null) params = params.set('maxDuration', filter.maxDuration);
      if (filter.age != null) params = params.set('age', filter.age);
      if (filter.sort) params = params.set('sort', filter.sort);
      for (const tagId of filter.tags ?? []) {
        params = params.append('tags', tagId);
      }
    }

    return this.http.get<Game[]>(this.baseUrl, { params });
  }

  getGame(id: number): Observable<Game> {
    return this.http.get<Game>(`${this.baseUrl}/${id}`);
  }

  /** Jeux suggérés sur la fiche d'un jeu : extension/jeu de base, même éditeur, tags ou catégorie en commun. */
  getRelatedGames(id: number): Observable<Game[]> {
    return this.http.get<Game[]>(`${this.baseUrl}/${id}/related`);
  }
}
