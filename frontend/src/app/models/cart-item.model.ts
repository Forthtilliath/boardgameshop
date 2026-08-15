import { Game } from './game.model';

export interface CartItem {
  game: Game;
  quantity: number;
}
