import { computed, inject, Injectable, signal } from '@angular/core';

import type { CartItem } from '../models/cart-item.model';
import type { Game } from '../models/game.model';
import { ToastService } from './toast.service';

const STORAGE_KEY = 'bgs-cart';

/**
 * Panier gere entierement cote client (signals + localStorage), sans compte
 * utilisateur : il persiste d'une visite a l'autre sur le meme navigateur.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly toastService = inject(ToastService);

  private readonly itemsSignal = signal<CartItem[]>(this.loadFromStorage());

  readonly items = this.itemsSignal.asReadonly();

  readonly itemCount = computed(() =>
    this.itemsSignal().reduce((total, item) => total + item.quantity, 0)
  );

  readonly total = computed(() =>
    this.itemsSignal().reduce((sum, item) => sum + item.game.price * item.quantity, 0)
  );

  addToCart(game: Game, quantity = 1): void {
    this.itemsSignal.update((items) => {
      const existing = items.find((item) => item.game.id === game.id);
      if (existing) {
        return items.map((item) =>
          item.game.id === game.id ? { ...item, quantity: item.quantity + quantity } : item
        );
      }
      return [...items, { game, quantity }];
    });
    this.persist();
    this.toastService.success(`${game.name} ajouté au panier`);
  }

  updateQuantity(gameId: number, quantity: number): void {
    if (quantity <= 0) {
      // Silencieux : atteindre 0 via le stepper "-" est une interaction courante
      // et attendue, pas une suppression volontaire méritant une notification.
      this.removeFromCart(gameId, { silent: true });
      return;
    }
    this.itemsSignal.update((items) =>
      items.map((item) => (item.game.id === gameId ? { ...item, quantity } : item))
    );
    this.persist();
  }

  removeFromCart(gameId: number, options?: { silent?: boolean }): void {
    const removedItem = this.itemsSignal().find((item) => item.game.id === gameId);
    this.itemsSignal.update((items) => items.filter((item) => item.game.id !== gameId));
    this.persist();
    if (!options?.silent && removedItem) {
      this.toastService.info(`${removedItem.game.name} retiré du panier`);
    }
  }

  clear(): void {
    this.itemsSignal.set([]);
    this.persist();
  }

  private persist(): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.itemsSignal()));
  }

  private loadFromStorage(): CartItem[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as CartItem[]) : [];
    } catch {
      return [];
    }
  }
}
