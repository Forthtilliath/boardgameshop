import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, expand, switchMap, timer } from 'rxjs';

import { OrderResponse } from '../../models/order.model';
import { OrderService } from '../../services/order.service';

const POLL_INTERVAL_MS = 1500;
const MAX_POLLS = 5;

/**
 * Le paiement est confirme cote serveur de facon asynchrone par le webhook
 * Stripe : si la commande est encore EN_ATTENTE_PAIEMENT a l'affichage, on
 * reinterroge quelques fois avant d'abandonner (le webhook met generalement
 * moins d'une seconde en local via `stripe listen`).
 */
@Component({
  selector: 'app-order-confirmation',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './order-confirmation.component.html',
  styleUrl: './order-confirmation.component.scss'
})
export class OrderConfirmationComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly orderService = inject(OrderService);

  readonly order = signal<OrderResponse | null>(null);
  readonly notFound = signal(false);

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.orderService
      .getOrder(id)
      .pipe(
        expand((order, index) =>
          order.status !== 'EN_ATTENTE_PAIEMENT' || index >= MAX_POLLS
            ? EMPTY
            : timer(POLL_INTERVAL_MS).pipe(switchMap(() => this.orderService.getOrder(id)))
        )
      )
      .subscribe({
        next: (order) => this.order.set(order),
        error: () => this.notFound.set(true)
      });
  }
}
