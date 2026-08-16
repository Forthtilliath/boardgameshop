import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { AdminStats } from '../models/admin-stats.model';

@Injectable({ providedIn: 'root' })
export class AdminStatsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/stats`;

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(this.baseUrl);
  }
}
