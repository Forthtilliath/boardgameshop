import { Injectable, computed, signal } from '@angular/core';

import { CartItem } from '../models/cart-item.model';
import { Game } from '../models/game.model';

const STORAGE_KEY = 'bgs-cart';

/**
 * Panier gere entierement cote client (signals + localStorage), sans compte
 * utilisateur : il persiste d'une visite a l'autre sur le meme navigateur.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
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
  }

  updateQuantity(gameId: number, quantity: number): void {
    if (quantity <= 0) {
      this.removeFromCart(gameId);
      return;
    }
    this.itemsSignal.update((items) =>
      items.map((item) => (item.game.id === gameId ? { ...item, quantity } : item))
    );
    this.persist();
  }

  removeFromCart(gameId: number): void {
    this.itemsSignal.update((items) => items.filter((item) => item.game.id !== gameId));
    this.persist();
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
