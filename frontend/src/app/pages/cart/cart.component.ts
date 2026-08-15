import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { OrderResponse } from '../../models/order.model';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-cart',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent {
  private readonly cartService = inject(CartService);
  private readonly orderService = inject(OrderService);

  readonly items = this.cartService.items;
  readonly total = this.cartService.total;

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly confirmedOrder = signal<OrderResponse | null>(null);

  updateQuantity(gameId: number, quantity: number): void {
    this.cartService.updateQuantity(gameId, quantity);
  }

  remove(gameId: number): void {
    this.cartService.removeFromCart(gameId);
  }

  checkout(): void {
    const items = this.cartService.items();
    if (items.length === 0) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.orderService
      .createOrder({
        items: items.map((item) => ({ gameId: item.game.id, quantity: item.quantity }))
      })
      .subscribe({
        next: (order) => {
          this.confirmedOrder.set(order);
          this.cartService.clear();
          this.submitting.set(false);
        },
        error: (err) => {
          this.errorMessage.set(
            err?.error?.message ?? 'La commande n\'a pas pu etre validee. Reessayez.'
          );
          this.submitting.set(false);
        }
      });
  }
}
