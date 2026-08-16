import { Tag } from './tag.model';

export interface Game {
  id: number;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl: string;
  publisher: string;
  minPlayers: number;
  maxPlayers: number;
  durationMinutes: number;
  stock: number;
  minAge: number | null;
  releaseDate: string | null;
  discountPercent: number | null;
  discountEndsAt: string | null;
  finalPrice: number;
  onSale: boolean;
  preorder: boolean;
  tags: Tag[];
}

/** Payload envoye par le dashboard admin pour creer/modifier un jeu. */
export interface AdminGameRequest {
  name: string;
  description: string | null;
  price: number;
  category: string | null;
  imageUrl: string | null;
  publisher: string | null;
  minPlayers: number | null;
  maxPlayers: number | null;
  durationMinutes: number | null;
  stock: number;
  minAge: number | null;
  releaseDate: string | null;
  discountPercent: number | null;
  discountEndsAt: string | null;
  tagIds: number[];
}
