export interface CreatePaymentIntentRequest {
  orderId: number;
}

export interface PaymentIntentResponse {
  clientSecret: string;
}
