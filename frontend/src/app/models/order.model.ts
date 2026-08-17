export interface OrderItemRequest {
  gameId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  items: OrderItemRequest[];
  /** Revalidé côté serveur : voir OrderService#createOrder côté back. */
  promoCode?: string;
}

export interface OrderLineResponse {
  gameId: number;
  gameName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface OrderResponse {
  id: number;
  createdAt: string;
  status: string;
  lines: OrderLineResponse[];
  totalAmount: number;
  promoCode: string | null;
  discountAmount: number | null;
}

export type OrderStatus =
  | 'EN_ATTENTE_PAIEMENT'
  | 'PAYEE'
  | 'ECHOUEE'
  | 'EXPEDIEE'
  | 'LIVREE'
  | 'ANNULEE';

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  EN_ATTENTE_PAIEMENT: 'En attente de paiement',
  PAYEE: 'Payée',
  ECHOUEE: 'Paiement échoué',
  EXPEDIEE: 'Expédiée',
  LIVREE: 'Livrée',
  ANNULEE: 'Annulée'
};

/** Statuts pour lesquels une facture PDF est disponible (voir OrderService#downloadInvoice côté back). */
export const INVOICEABLE_STATUSES: OrderStatus[] = ['PAYEE', 'EXPEDIEE', 'LIVREE'];

export function orderStatusLabel(status: string): string {
  // Cast défensif : `status` vient du backend et n'est pas garanti d'être une
  // valeur connue de OrderStatus, même si le typage le suppose.
  return ORDER_STATUS_LABELS[status as OrderStatus] ?? status;
}

export interface AdminOrderResponse extends OrderResponse {
  userId: number;
  userEmail: string;
  userFullName: string;
}
