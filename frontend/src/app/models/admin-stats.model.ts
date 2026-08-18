export interface TopSellingGame {
  gameId: number;
  gameName: string;
  quantitySold: number;
  revenue: number;
}

export interface BestOrder {
  orderId: number;
  customerName: string;
  totalAmount: number;
  createdAt: string;
}

export interface DailyOrderCount {
  date: string;
  count: number;
}

export interface DailyRevenue {
  date: string;
  revenue: number;
}

export interface AdminStats {
  totalRevenue: number;
  totalOrders: number;
  averageOrderAmount: number;
  /** Commandes en attente de paiement : proxy le plus proche de "paniers en cours"
   *  mesurable côté serveur (le panier lui-même n'est jamais persisté en base). */
  pendingPaymentOrders: number;
  bestOrder: BestOrder | null;
  ordersByDay: DailyOrderCount[];
  revenueByDay: DailyRevenue[];
  topSellingGames: TopSellingGame[];
}
