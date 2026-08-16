export interface TopSellingGame {
  gameId: number;
  gameName: string;
  quantitySold: number;
  revenue: number;
}

export interface AdminStats {
  totalRevenue: number;
  totalOrders: number;
  topSellingGames: TopSellingGame[];
}
