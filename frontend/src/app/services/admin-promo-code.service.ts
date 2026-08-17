import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type { AdminPromoCodeRequest, AdminPromoCodeResponse } from '../models/promo-code.model';

@Injectable({ providedIn: 'root' })
export class AdminPromoCodeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/promo-codes`;

  getPromoCodes(): Observable<AdminPromoCodeResponse[]> {
    return this.http.get<AdminPromoCodeResponse[]>(this.baseUrl);
  }

  createPromoCode(request: AdminPromoCodeRequest): Observable<AdminPromoCodeResponse> {
    return this.http.post<AdminPromoCodeResponse>(this.baseUrl, request);
  }

  updatePromoCode(id: number, request: AdminPromoCodeRequest): Observable<AdminPromoCodeResponse> {
    return this.http.put<AdminPromoCodeResponse>(`${this.baseUrl}/${id}`, request);
  }

  deletePromoCode(id: number): Observable<void> {
    // Seule façon de typer une réponse HTTP sans corps avec HttpClient.
    // eslint-disable-next-line @typescript-eslint/no-invalid-void-type
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
