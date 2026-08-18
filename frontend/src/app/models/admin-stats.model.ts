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

export interface StatusCount {
  status: string;
  count: number;
}

export interface CategoryRevenue {
  category: string;
  revenue: number;
}

export interface PromoCodeUsage {
  code: string;
  usesCount: number;
  maxUses: number | null;
  active: boolean;
}

export interface LowStockGame {
  gameId: number;
  gameName: string;
  stock: number;
}

export interface TopFavoriteGame {
  gameId: number;
  gameName: string;
  favoriteCount: number;
}

export interface RestockDemandGame {
  gameId: number;
  gameName: string;
  alertCount: number;
}

export interface RatingBucket {
  stars: number;
  count: number;
}

export interface TopRatedGame {
  gameId: number;
  gameName: string;
  averageRating: number;
  reviewsCount: number;
}

export interface MostReviewedGame {
  gameId: number;
  gameName: string;
  reviewsCount: number;
}

export interface DailySignupCount {
  date: string;
  count: number;
}

export interface TopCustomer {
  userId: number;
  customerName: string;
  totalSpent: number;
  orderCount: number;
}

export interface SalesStats {
  totalRevenue: number;
  totalOrders: number;
  averageOrderAmount: number;
  pendingPaymentOrders: number;
  bestOrder: BestOrder | null;
  ordersByDay: DailyOrderCount[];
  revenueByDay: DailyRevenue[];
  topSellingGames: TopSellingGame[];
  ordersByStatus: StatusCount[];
  revenueByCategory: CategoryRevenue[];
  promoCodeUsage: PromoCodeUsage[];
  totalDiscountGiven: number;
}

export interface CatalogStats {
  totalStockValue: number;
  outOfStockCount: number;
  lowStockCount: number;
  lowStockGames: LowStockGame[];
  activePromotionsCount: number;
  topFavoriteGames: TopFavoriteGame[];
  restockDemand: RestockDemandGame[];
}

export interface ReviewStats {
  averageRating: number | null;
  totalReviews: number;
  ratingDistribution: RatingBucket[];
  topRatedGames: TopRatedGame[];
  worstRatedGames: TopRatedGame[];
  mostReviewedGames: MostReviewedGame[];
}

export interface UserStats {
  totalUsers: number;
  signupsByDay: DailySignupCount[];
  topCustomers: TopCustomer[];
  conversionRate: number;
}

export interface AdminStats {
  sales: SalesStats;
  catalog: CatalogStats;
  reviews: ReviewStats;
  users: UserStats;
}
