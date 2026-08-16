export interface OrderItemRequest {
  gameId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  items: OrderItemRequest[];
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
}

export type OrderStatus =
  | 'EN_ATTENTE_PAIEMENT'
  | 'PAYEE'
  | 'ECHOUEE'
  | 'EXPEDIEE'
  | 'LIVREE'
  | 'ANNULEE';

export interface AdminOrderResponse extends OrderResponse {
  userId: number;
  userEmail: string;
  userFullName: string;
}
