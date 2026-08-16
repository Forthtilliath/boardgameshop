import { CurrencyPipe } from '@angular/common';
import { Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import type { Stripe, StripeElements } from '@stripe/stripe-js';

import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { PaymentService } from '../../services/payment.service';

/**
 * Cree la commande (statut EN_ATTENTE_PAIEMENT), initialise le Payment Intent
 * Stripe correspondant, puis monte le Payment Element pour saisir la carte.
 * Le statut definitif (PAYEE/ECHOUEE) est confirme cote serveur par le webhook
 * Stripe ; cette page redirige vers la confirmation une fois le paiement soumis.
 */
@Component({
  selector: 'app-checkout',
  imports: [CurrencyPipe],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.scss'
})
export class CheckoutComponent implements OnInit {
  private readonly cartService = inject(CartService);
  private readonly orderService = inject(OrderService);
  private readonly paymentService = inject(PaymentService);
  private readonly router = inject(Router);

  readonly paymentElementContainer = viewChild.required<ElementRef<HTMLDivElement>>('paymentElement');

  readonly items = this.cartService.items;
  readonly total = this.cartService.total;

  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  private stripe: Stripe | null = null;
  private elements: StripeElements | null = null;
  private orderId: number | null = null;

  ngOnInit(): void {
    const items = this.cartService.items();
    if (items.length === 0) {
      this.router.navigateByUrl('/panier');
      return;
    }

    this.orderService
      .createOrder({
        items: items.map((item) => ({ gameId: item.game.id, quantity: item.quantity }))
      })
      .subscribe({
        next: (order) => {
          this.orderId = order.id;
          this.setupPayment(order.id);
        },
        error: (err) => {
          this.errorMessage.set(err?.error?.message ?? 'Impossible de creer la commande.');
          this.loading.set(false);
        }
      });
  }

  async submit(): Promise<void> {
    if (!this.stripe || !this.elements || this.orderId === null) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const { error } = await this.stripe.confirmPayment({
      elements: this.elements,
      confirmParams: {
        return_url: `${window.location.origin}/confirmation/${this.orderId}`
      },
      redirect: 'if_required'
    });

    if (error) {
      this.errorMessage.set(error.message ?? 'Le paiement a echoue. Reessayez.');
      this.submitting.set(false);
      return;
    }

    this.cartService.clear();
    this.router.navigateByUrl(`/confirmation/${this.orderId}`);
  }

  private setupPayment(orderId: number): void {
    this.paymentService.createPaymentIntent({ orderId }).subscribe({
      next: (intentResponse) => {
        this.paymentService.getStripe().subscribe((stripe) => {
          if (!stripe) {
            this.errorMessage.set("Le module de paiement n'a pas pu se charger.");
            this.loading.set(false);
            return;
          }

          this.stripe = stripe;
          this.elements = stripe.elements({ clientSecret: intentResponse.clientSecret });
          this.elements.create('payment').mount(this.paymentElementContainer().nativeElement);
          this.loading.set(false);
        });
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? "Impossible d'initialiser le paiement.");
        this.loading.set(false);
      }
    });
  }
}
