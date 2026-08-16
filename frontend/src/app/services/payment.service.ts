import { HttpClient } from '@angular/common/http';
import { inject,Injectable } from '@angular/core';
import type {Stripe } from '@stripe/stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import type { Observable} from 'rxjs';
import { from, shareReplay } from 'rxjs';

import { environment } from '../../environments/environment';
import type { CreatePaymentIntentRequest, PaymentIntentResponse } from '../models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/payments`;

  private stripePromise: Observable<Stripe | null> | null = null;

  /**
   * Charge le SDK Stripe.js une seule fois (mis en cache pour le reste de la session).
   */
  getStripe(): Observable<Stripe | null> {
    this.stripePromise ??= from(loadStripe(environment.stripePublicKey)).pipe(shareReplay(1));
    return this.stripePromise;
  }

  createPaymentIntent(request: CreatePaymentIntentRequest): Observable<PaymentIntentResponse> {
    return this.http.post<PaymentIntentResponse>(`${this.baseUrl}/create-intent`, request);
  }
}
