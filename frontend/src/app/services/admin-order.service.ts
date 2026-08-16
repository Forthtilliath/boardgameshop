import { HttpClient } from '@angular/common/http';
import { inject,Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type { AdminOrderResponse, OrderStatus } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class AdminOrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/orders`;

  getOrders(status?: OrderStatus): Observable<AdminOrderResponse[]> {
    if (status) {
      return this.http.get<AdminOrderResponse[]>(this.baseUrl, { params: { status } });
    }
    return this.http.get<AdminOrderResponse[]>(this.baseUrl);
  }

  updateStatus(id: number, status: OrderStatus): Observable<AdminOrderResponse> {
    return this.http.put<AdminOrderResponse>(`${this.baseUrl}/${id}/status`, { status });
  }
}
