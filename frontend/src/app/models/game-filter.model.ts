export type GameSort = 'newest' | 'price_asc' | 'price_desc' | 'popularity' | 'rating';

export interface GameFilter {
  category?: string;
  priceMin?: number;
  priceMax?: number;
  players?: number;
  maxDuration?: number;
  age?: number;
  tags?: number[];
  sort?: GameSort;
  search?: string;
}
