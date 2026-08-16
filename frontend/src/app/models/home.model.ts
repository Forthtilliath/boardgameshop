import type { Game } from './game.model';
import type { Review } from './review.model';

export interface Home {
  newest: Game[];
  bestSellers: Game[];
  preorders: Game[];
  onSale: Game[];
  recentReviews: Review[];
}
