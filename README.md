# BGS - BoardGameShop

Site fictif de vente de jeux de société, construit avec **Angular** (frontend) et **Java / Spring Boot** (backend), pour explorer ces deux stacks en profondeur (comptes utilisateurs, paiement, dashboard admin, etc.).

## Structure du dépôt

```
boardgameshop/
├── frontend/   Application Angular (catalogue, fiche jeu, panier, compte, paiement)
└── backend/    API REST Spring Boot (catalogue, commandes, authentification, paiement)
```

## Fonctionnalités

- Page d'accueil (nouveautés, meilleures ventes, précommandes, promotions, avis récents)
- Catalogue de jeux de société avec filtres (prix, joueurs, durée, âge, tags) et tri
- Fiche détaillée de chaque jeu, avec avis clients (achat vérifié)
- Comptes utilisateurs (inscription/connexion, JWT), favoris
- Panier persistant (localStorage) ; le passage de commande nécessite un compte
- Paiement par carte bancaire via **Stripe** (mode test), avec suivi du statut de la commande
- Dashboard admin : gestion des jeux, des commandes, des utilisateurs, statistiques de vente

## Prérequis

- Node.js 22+ et npm
- Java 21
- (Maven n'est pas requis : le wrapper `mvnw` / `mvnw.cmd` est inclus)
- Docker (pour la base PostgreSQL — voir plus bas)
- Un compte Stripe gratuit (mode test) pour le paiement — voir plus bas

## Démarrer la base de données

```bash
docker compose up -d
```

Lance un PostgreSQL local (identifiants de dev dans `docker-compose.yml`, sans conséquence
car uniquement accessible en local). À faire une fois avant de démarrer le backend ; les
données persistent ensuite dans un volume Docker nommé entre les redémarrages.

## Démarrer les deux serveurs en une commande

```bash
npm install   # une seule fois, à la racine
npm run dev
```

Lance le backend (`:8080`) et le frontend (`:4200`) en parallèle. Voir aussi les commandes
individuelles ci-dessous si besoin de les lancer séparément.

## Démarrer le backend seul

```bash
cd backend
./mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # Linux / macOS
```

L'API démarre sur `http://localhost:8080` (nécessite `docker compose up -d` au préalable).
La base est peuplée automatiquement avec un jeu de données de démonstration au premier
démarrage (catalogue + comptes de test ci-dessous).

### Comptes de démonstration

| Email            | Mot de passe | Rôle  |
|------------------|--------------|-------|
| `admin@bgs.fr`   | `admin1234`  | ADMIN |
| `user@bgs.fr`    | `user1234`   | USER  |

## Démarrer le frontend seul

```bash
cd frontend
npm install
npm start
```

L'application est disponible sur `http://localhost:4200` et appelle l'API sur `http://localhost:8080/api`.

## Configurer Stripe (paiement en mode test)

1. Crée un compte gratuit sur [stripe.com](https://stripe.com) si tu n'en as pas.
2. Passe en mode test / sandbox dans le dashboard, puis va sur
   [dashboard.stripe.com/test/apikeys](https://dashboard.stripe.com/test/apikeys) pour récupérer :
   - la **clé publique** `pk_test_...` : à coller dans
     `frontend/src/environments/environment.development.ts` (`stripePublicKey`)
   - la **clé secrète** `sk_test_...` : à poser en variable d'environnement, jamais dans un fichier
     versionné :
     ```powershell
     setx STRIPE_SECRET_KEY "sk_test_..."   # rouvrir un terminal ensuite
     ```
3. Pour recevoir les webhooks Stripe en local (confirmation de paiement asynchrone), installe la
   [Stripe CLI](https://docs.stripe.com/stripe-cli) puis lance, en parallèle du backend :
   ```bash
   stripe listen --forward-to localhost:8080/api/payments/webhook
   ```
   La commande affiche un secret `whsec_...` à poser également en variable d'environnement :
   ```powershell
   setx STRIPE_WEBHOOK_SECRET "whsec_..."
   ```

### Cartes de test Stripe

| Numéro                 | Résultat                    |
|-------------------------|------------------------------|
| `4242 4242 4242 4242`   | Paiement accepté              |
| `4000 0000 0000 0002`   | Carte refusée                 |

Date d'expiration : n'importe quelle date future. CVC : n'importe quel 3 chiffres.

## Principaux endpoints de l'API

| Méthode | URL                                | Description                                    | Auth requise |
|---------|-------------------------------------|-------------------------------------------------|:---:|
| GET     | `/api/games`                        | Liste des jeux (filtres, tri)                    | non |
| GET     | `/api/games/{id}`                   | Détail d'un jeu                                  | non |
| GET     | `/api/tags`                         | Liste des tags (filtres du catalogue)            | non |
| GET     | `/api/home`                         | Agrégation page d'accueil                        | non |
| GET     | `/api/games/{id}/reviews`           | Avis d'un jeu                                    | non |
| GET     | `/api/games/{id}/reviews/can-review`| Vérifie si l'utilisateur peut laisser un avis     | oui |
| POST    | `/api/games/{id}/reviews`           | Publier un avis (achat vérifié)                  | oui |
| POST    | `/api/auth/register`                | Créer un compte                                  | non |
| POST    | `/api/auth/login`                   | Se connecter (retourne un JWT)                   | non |
| GET     | `/api/auth/me`                      | Utilisateur courant                              | oui |
| GET/POST/DELETE | `/api/favorites`, `/api/favorites/{gameId}` | Gérer ses favoris              | oui |
| POST    | `/api/orders`                       | Créer une commande à partir du panier            | oui |
| GET     | `/api/orders/{id}`                  | Consulter une commande (propriétaire/admin)      | oui |
| POST    | `/api/payments/create-intent`       | Initialiser le paiement Stripe d'une commande     | oui |
| POST    | `/api/payments/webhook`             | Appelé par Stripe (signature vérifiée)           | non (signature) |
| CRUD    | `/api/admin/games`, `/api/admin/tags` | Gestion du catalogue (dashboard admin)         | ADMIN |
| GET/PUT | `/api/admin/orders`                 | Gestion des commandes (dashboard admin)          | ADMIN |
| GET     | `/api/admin/users`                  | Liste des utilisateurs (dashboard admin)         | ADMIN |
| GET     | `/api/admin/stats`                  | Statistiques de vente (dashboard admin)          | ADMIN |

## Stack technique

- **Frontend** : Angular 19, composants standalone, signals, SCSS, Stripe.js
- **Backend** : Spring Boot 4.1, Java 21, Spring Security (JWT), Spring Data JPA, PostgreSQL, Stripe Java SDK, Bean Validation, Lombok, Maven
