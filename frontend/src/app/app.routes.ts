import type { Routes } from '@angular/router';

import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';
import { AccountComponent } from './pages/account/account.component';
import { AdminGameFormComponent } from './pages/admin/admin-game-form/admin-game-form.component';
import { AdminGamesListComponent } from './pages/admin/admin-games-list/admin-games-list.component';
import { AdminLayoutComponent } from './pages/admin/admin-layout/admin-layout.component';
import { AdminOrdersListComponent } from './pages/admin/admin-orders-list/admin-orders-list.component';
import { AdminPromoCodeFormComponent } from './pages/admin/admin-promo-code-form/admin-promo-code-form.component';
import { AdminPromoCodesListComponent } from './pages/admin/admin-promo-codes-list/admin-promo-codes-list.component';
import { AdminStatsComponent } from './pages/admin/admin-stats/admin-stats.component';
import { AdminUsersListComponent } from './pages/admin/admin-users-list/admin-users-list.component';
import { CartComponent } from './pages/cart/cart.component';
import { CatalogComponent } from './pages/catalog/catalog.component';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { FavoritesComponent } from './pages/favorites/favorites.component';
import { GameDetailComponent } from './pages/game-detail/game-detail.component';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';
import { OrderConfirmationComponent } from './pages/order-confirmation/order-confirmation.component';
import { RegisterComponent } from './pages/register/register.component';
import { SharedFavoritesComponent } from './pages/shared-favorites/shared-favorites.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, title: 'BGS - Accueil' },
  { path: 'jeux', component: CatalogComponent, title: 'BGS - Catalogue' },
  { path: 'jeux/:id', component: GameDetailComponent, title: 'BGS - Détail du jeu' },
  { path: 'panier', component: CartComponent, title: 'BGS - Panier' },
  { path: 'favoris', component: FavoritesComponent, title: 'BGS - Mes favoris', canActivate: [authGuard] },
  {
    path: 'favoris/partages/:token',
    component: SharedFavoritesComponent,
    title: 'BGS - Favoris partagés'
  },
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
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [adminGuard],
    children: [
      { path: '', component: AdminStatsComponent, title: 'BGS Admin - Tableau de bord' },
      { path: 'jeux', component: AdminGamesListComponent, title: 'BGS Admin - Jeux' },
      { path: 'jeux/nouveau', component: AdminGameFormComponent, title: 'BGS Admin - Nouveau jeu' },
      { path: 'jeux/:id/modifier', component: AdminGameFormComponent, title: 'BGS Admin - Modifier le jeu' },
      { path: 'commandes', component: AdminOrdersListComponent, title: 'BGS Admin - Commandes' },
      { path: 'codes-promo', component: AdminPromoCodesListComponent, title: 'BGS Admin - Codes promo' },
      {
        path: 'codes-promo/nouveau',
        component: AdminPromoCodeFormComponent,
        title: 'BGS Admin - Nouveau code promo'
      },
      {
        path: 'codes-promo/:id/modifier',
        component: AdminPromoCodeFormComponent,
        title: 'BGS Admin - Modifier le code promo'
      },
      { path: 'utilisateurs', component: AdminUsersListComponent, title: 'BGS Admin - Utilisateurs' }
    ]
  },
  { path: '**', component: NotFoundComponent, title: 'BGS - Page introuvable' }
];
