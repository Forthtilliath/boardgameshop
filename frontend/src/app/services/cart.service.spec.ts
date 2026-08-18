import { TestBed } from '@angular/core/testing';

import type { Game } from '../models/game.model';
import { CartService } from './cart.service';

function makeGame(overrides: Partial<Game> = {}): Game {
  return {
    id: 1,
    name: 'Catane',
    description: '',
    price: 30,
    category: 'Stratégie',
    imageUrl: '',
    publisher: 'Kosmos',
    minPlayers: 3,
    maxPlayers: 4,
    durationMinutes: 90,
    stock: 10,
    minAge: 10,
    releaseDate: null,
    discountPercent: null,
    discountEndsAt: null,
    finalPrice: 30,
    onSale: false,
    preorder: false,
    tags: [],
    reviewsAverage: null,
    reviewsCount: 0,
    ...overrides
  };
}

describe('CartService', () => {
  let service: CartService;

  beforeEach(() => {
    localStorage.removeItem('bgs-cart');
    TestBed.configureTestingModule({});
    service = TestBed.inject(CartService);
  });

  afterEach(() => {
    localStorage.removeItem('bgs-cart');
  });

  it('starts empty', () => {
    expect(service.items()).toEqual([]);
    expect(service.itemCount()).toBe(0);
    expect(service.total()).toBe(0);
  });

  it('adds a new game to the cart', () => {
    service.addToCart(makeGame(), 2);

    expect(service.items().length).toBe(1);
    expect(service.itemCount()).toBe(2);
    expect(service.total()).toBe(60);
  });

  it('increments quantity when adding the same game twice', () => {
    service.addToCart(makeGame(), 1);
    service.addToCart(makeGame(), 2);

    expect(service.items().length).toBe(1);
    expect(service.items()[0].quantity).toBe(3);
  });

  it('updates the quantity of an existing item', () => {
    service.addToCart(makeGame());
    service.updateQuantity(1, 5);

    expect(service.items()[0].quantity).toBe(5);
  });

  it('removes the item when the quantity drops to zero', () => {
    service.addToCart(makeGame());
    service.updateQuantity(1, 0);

    expect(service.items()).toEqual([]);
  });

  it('removes an item explicitly', () => {
    service.addToCart(makeGame({ id: 1 }));
    service.addToCart(makeGame({ id: 2, name: 'Carcassonne' }));
    service.removeFromCart(1);

    expect(service.items().length).toBe(1);
    expect(service.items()[0].game.id).toBe(2);
  });

  it('clears the cart', () => {
    service.addToCart(makeGame());
    service.clear();

    expect(service.items()).toEqual([]);
  });

  it('persists the cart across service instances (localStorage)', () => {
    service.addToCart(makeGame(), 3);

    // Un nouvel injecteur force une nouvelle instance (providedIn: 'root' ne
    // recrée pas le service sans ça), pour vérifier une vraie relecture du
    // localStorage plutôt que de retomber sur le même singleton en mémoire.
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    const reloaded = TestBed.inject(CartService);

    expect(reloaded.items().length).toBe(1);
    expect(reloaded.items()[0].quantity).toBe(3);
  });
});
