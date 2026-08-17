import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type { PromoCodeResponse } from '../models/promo-code.model';

@Injectable({ providedIn: 'root' })
export class PromoCodeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/promo-codes`;

  /** Prévisualise la réduction d'un code, avant de passer commande. */
  validate(code: string): Observable<PromoCodeResponse> {
    return this.http.post<PromoCodeResponse>(`${this.baseUrl}/validate`, { code });
  }
}
