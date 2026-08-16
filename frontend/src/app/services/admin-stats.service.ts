import { HttpClient } from '@angular/common/http';
import { inject,Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type { AdminStats } from '../models/admin-stats.model';

@Injectable({ providedIn: 'root' })
export class AdminStatsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/stats`;

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(this.baseUrl);
  }
}
