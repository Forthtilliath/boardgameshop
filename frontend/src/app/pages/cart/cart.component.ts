import { CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-cart',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent {
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);

  readonly items = this.cartService.items;
  readonly total = this.cartService.total;

  updateQuantity(gameId: number, quantity: number): void {
    this.cartService.updateQuantity(gameId, quantity);
  }

  remove(gameId: number): void {
    this.cartService.removeFromCart(gameId);
  }

  checkout(): void {
    if (this.cartService.items().length === 0) {
      return;
    }
    // authGuard sur /commande redirige vers /connexion?returnUrl=/commande si besoin
    this.router.navigateByUrl('/commande');
  }
}
