export interface Review {
  id: number;
  gameId: number;
  gameName: string;
  userFirstName: string;
  rating: number;
  comment: string | null;
  createdAt: string;
}

export interface CreateReviewRequest {
  rating: number;
  comment: string | null;
}
