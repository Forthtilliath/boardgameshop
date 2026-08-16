import { Routes } from '@angular/router';

import { authGuard } from './guards/auth.guard';
import { AccountComponent } from './pages/account/account.component';
import { CartComponent } from './pages/cart/cart.component';
import { CatalogComponent } from './pages/catalog/catalog.component';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { GameDetailComponent } from './pages/game-detail/game-detail.component';
import { LoginComponent } from './pages/login/login.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';
import { OrderConfirmationComponent } from './pages/order-confirmation/order-confirmation.component';
import { RegisterComponent } from './pages/register/register.component';

export const routes: Routes = [
  { path: '', component: CatalogComponent, title: 'BGS - Catalogue' },
  { path: 'jeux/:id', component: GameDetailComponent, title: 'BGS - Detail du jeu' },
  { path: 'panier', component: CartComponent, title: 'BGS - Panier' },
  { path: 'commande', component: CheckoutComponent, title: 'BGS - Paiement', canActivate: [authGuard] },
  {
    path: 'confirmation/:id',
    component: OrderConfirmationComponent,
    title: 'BGS - Confirmation',
    canActivate: [authGuard]
  },
  { path: 'connexion', component: LoginComponent, title: 'BGS - Connexion' },
  { path: 'inscription', component: RegisterComponent, title: 'BGS - Inscription' },
  { path: 'mon-compte', component: AccountComponent, title: 'BGS - Mon compte', canActivate: [authGuard] },
  { path: '**', component: NotFoundComponent, title: 'BGS - Page introuvable' }
];
