import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import type { OrderResponse, OrderStatus } from '../../models/order.model';
import { INVOICEABLE_STATUSES, orderStatusLabel } from '../../models/order.model';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-account',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './account.component.html',
  styleUrl: './account.component.scss'
})
export class AccountComponent {
  private readonly authService = inject(AuthService);
  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);

  readonly user = this.authService.currentUser;

  readonly orders = signal<OrderResponse[]>([]);
  readonly loadingOrders = signal(true);
  readonly expandedOrderId = signal<number | null>(null);
  readonly downloadingInvoiceId = signal<number | null>(null);

  constructor() {
    this.orderService.listMyOrders().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loadingOrders.set(false);
      },
      error: () => { this.loadingOrders.set(false); }
    });
  }

  statusLabel(status: string): string {
    return orderStatusLabel(status);
  }

  canDownloadInvoice(status: string): boolean {
    return INVOICEABLE_STATUSES.includes(status as OrderStatus);
  }

  toggleOrder(orderId: number): void {
    this.expandedOrderId.set(this.expandedOrderId() === orderId ? null : orderId);
  }

  downloadInvoice(order: OrderResponse): void {
    this.downloadingInvoiceId.set(order.id);
    this.orderService.downloadInvoice(order.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `facture-bgs-${order.id}.pdf`;
        link.click();
        URL.revokeObjectURL(url);
        this.downloadingInvoiceId.set(null);
      },
      error: () => { this.downloadingInvoiceId.set(null); }
    });
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/');
  }
}
