import { Routes } from '@angular/router';

import { CartComponent } from './pages/cart/cart.component';
import { CatalogComponent } from './pages/catalog/catalog.component';
import { GameDetailComponent } from './pages/game-detail/game-detail.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';

export const routes: Routes = [
  { path: '', component: CatalogComponent, title: 'BGS - Catalogue' },
  { path: 'jeux/:id', component: GameDetailComponent, title: 'BGS - Detail du jeu' },
  { path: 'panier', component: CartComponent, title: 'BGS - Panier' },
  { path: '**', component: NotFoundComponent, title: 'BGS - Page introuvable' }
];
