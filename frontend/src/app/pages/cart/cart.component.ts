import { CurrencyPipe } from '@angular/common';
import type { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import type { PromoCodeResponse } from '../../models/promo-code.model';
import { CartService } from '../../services/cart.service';
import { PromoCodeService } from '../../services/promo-code.service';

@Component({
  selector: 'app-cart',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent {
  private readonly cartService = inject(CartService);
  private readonly promoCodeService = inject(PromoCodeService);
  private readonly router = inject(Router);

  readonly items = this.cartService.items;
  readonly total = this.cartService.total;

  readonly promoCodeInput = signal('');
  readonly appliedPromoCode = signal<PromoCodeResponse | null>(null);
  readonly promoCodeError = signal<string | null>(null);
  readonly validatingPromoCode = signal(false);

  readonly discountAmount = computed(() => {
    const applied = this.appliedPromoCode();
    return applied ? this.total() * (applied.discountPercent / 100) : 0;
  });
  readonly discountedTotal = computed(() => this.total() - this.discountAmount());

  updateQuantity(gameId: number, quantity: number): void {
    this.cartService.updateQuantity(gameId, quantity);
  }

  remove(gameId: number): void {
    this.cartService.removeFromCart(gameId);
  }

  applyPromoCode(): void {
    const code = this.promoCodeInput().trim();
    if (!code) {
      return;
    }

    this.validatingPromoCode.set(true);
    this.promoCodeError.set(null);

    this.promoCodeService.validate(code).subscribe({
      next: (response) => {
        this.appliedPromoCode.set(response);
        this.validatingPromoCode.set(false);
      },
      error: (err: HttpErrorResponse) => {
        const message = (err.error as { message?: string } | null)?.message;
        this.appliedPromoCode.set(null);
        this.promoCodeError.set(message ?? 'Code promo invalide.');
        this.validatingPromoCode.set(false);
      }
    });
  }

  removePromoCode(): void {
    this.appliedPromoCode.set(null);
    this.promoCodeInput.set('');
    this.promoCodeError.set(null);
  }

  checkout(): void {
    if (this.cartService.items().length === 0) {
      return;
    }
    const applied = this.appliedPromoCode();
    // authGuard sur /commande redirige vers /connexion?returnUrl=/commande si besoin
    void this.router.navigate(['/commande'], {
      queryParams: applied ? { promo: applied.code } : {}
    });
  }
}
