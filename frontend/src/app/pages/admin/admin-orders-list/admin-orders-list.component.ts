import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';

import type { AdminOrderResponse, OrderStatus } from '../../../models/order.model';
import { orderStatusLabel } from '../../../models/order.model';
import { AdminOrderService } from '../../../services/admin-order.service';

const NEXT_STATUSES: Partial<Record<OrderStatus, OrderStatus[]>> = {
  PAYEE: ['EXPEDIEE', 'ANNULEE'],
  EXPEDIEE: ['LIVREE', 'ANNULEE']
};

@Component({
  selector: 'app-admin-orders-list',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './admin-orders-list.component.html',
  styleUrl: './admin-orders-list.component.scss'
})
export class AdminOrdersListComponent {
  private readonly adminOrderService = inject(AdminOrderService);

  readonly orders = signal<AdminOrderResponse[]>([]);
  readonly loading = signal(true);
  readonly updatingId = signal<number | null>(null);

  constructor() {
    this.loadOrders();
  }

  statusLabel(status: string): string {
    return orderStatusLabel(status);
  }

  nextStatuses(status: string): OrderStatus[] {
    return NEXT_STATUSES[status as OrderStatus] ?? [];
  }

  updateStatus(order: AdminOrderResponse, newStatus: OrderStatus): void {
    this.updatingId.set(order.id);
    this.adminOrderService.updateStatus(order.id, newStatus).subscribe({
      next: () => {
        this.updatingId.set(null);
        this.loadOrders();
      },
      error: () => { this.updatingId.set(null); }
    });
  }

  private loadOrders(): void {
    this.loading.set(true);
    this.adminOrderService.getOrders().subscribe((orders) => {
      this.orders.set(orders);
      this.loading.set(false);
    });
  }
}
