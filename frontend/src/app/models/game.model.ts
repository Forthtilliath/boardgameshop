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
}
