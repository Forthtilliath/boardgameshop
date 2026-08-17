import { Injectable, signal } from '@angular/core';

import type { Toast, ToastType } from '../models/toast.model';

const DEFAULT_DURATION_MS = 3000;

/**
 * Notifications globales (coin de l'écran, voir ToastContainerComponent monté dans
 * app.component.html). Injecté directement dans certains services transverses
 * (CartService, FavoriteService...) plutôt que répété dans chaque composant
 * appelant : évite de dupliquer le même appel de toast à chaque point d'entrée
 * (carte jeu, fiche jeu, slider de jeux liés...).
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly toastsSignal = signal<Toast[]>([]);
  readonly toasts = this.toastsSignal.asReadonly();

  private nextId = 0;

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error');
  }

  info(message: string): void {
    this.show(message, 'info');
  }

  dismiss(id: number): void {
    this.toastsSignal.update((toasts) => toasts.filter((t) => t.id !== id));
  }

  private show(message: string, type: ToastType): void {
    const id = this.nextId++;
    this.toastsSignal.update((toasts) => [...toasts, { id, message, type }]);
    setTimeout(() => { this.dismiss(id); }, DEFAULT_DURATION_MS);
  }
}
