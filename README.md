# BGS - BoardGameShop

Site fictif de vente de jeux de societe, construit avec **Angular** (frontend) et **Java / Spring Boot** (backend), pour explorer ces deux stacks en profondeur (comptes utilisateurs, paiement, dashboard admin, etc.).

## Structure du depot

```
boardgameshop/
├── frontend/   Application Angular (catalogue, fiche jeu, panier, compte, paiement)
└── backend/    API REST Spring Boot (catalogue, commandes, authentification, paiement)
```

## Fonctionnalites

- Catalogue de jeux de societe avec filtre par categorie
- Fiche detaillee de chaque jeu
- Comptes utilisateurs (inscription/connexion, JWT)
- Panier persistant (localStorage) ; le passage de commande necessite un compte
- Paiement par carte bancaire via **Stripe** (mode test), avec suivi du statut de la commande
- (a venir) dashboard admin, filtres avances, favoris, avis clients, page d'accueil enrichie

## Prerequis

- Node.js 22+ et npm
- Java 21
- (Maven n'est pas requis : le wrapper `mvnw` / `mvnw.cmd` est inclus)
- Un compte Stripe gratuit (mode test) pour le paiement — voir plus bas

## Demarrer les deux serveurs en une commande

```bash
npm install   # une seule fois, a la racine
npm run dev
```

Lance le backend (`:8080`) et le frontend (`:4200`) en parallele. Voir aussi les commandes
individuelles ci-dessous si besoin de les lancer separement.

## Demarrer le backend seul

```bash
cd backend
./mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # Linux / macOS
```

L'API demarre sur `http://localhost:8080`. Une base H2 sur fichier local (`backend/data/`, non
commitee) est peuplee automatiquement avec un jeu de donnees de demonstration au premier
demarrage (catalogue + comptes de test ci-dessous). Console H2 disponible sur `/h2-console`.

### Comptes de demonstration

| Email            | Mot de passe | Role  |
|------------------|--------------|-------|
| `admin@bgs.fr`   | `admin1234`  | ADMIN |
| `user@bgs.fr`    | `user1234`   | USER  |

## Demarrer le frontend seul

```bash
cd frontend
npm install
npm start
```

L'application est disponible sur `http://localhost:4200` et appelle l'API sur `http://localhost:8080/api`.

## Configurer Stripe (paiement en mode test)

1. Cree un compte gratuit sur [stripe.com](https://stripe.com) si tu n'en as pas.
2. Passe en mode test / sandbox dans le dashboard, puis va sur
   [dashboard.stripe.com/test/apikeys](https://dashboard.stripe.com/test/apikeys) pour recuperer :
   - la **cle publique** `pk_test_...` : a coller dans
     `frontend/src/environments/environment.development.ts` (`stripePublicKey`)
   - la **cle secrete** `sk_test_...` : a poser en variable d'environnement, jamais dans un fichier
     versionne :
     ```powershell
     setx STRIPE_SECRET_KEY "sk_test_..."   # rouvrir un terminal ensuite
     ```
3. Pour recevoir les webhooks Stripe en local (confirmation de paiement asynchrone), installe la
   [Stripe CLI](https://docs.stripe.com/stripe-cli) puis lance, en parallele du backend :
   ```bash
   stripe listen --forward-to localhost:8080/api/payments/webhook
   ```
   La commande affiche un secret `whsec_...` a poser egalement en variable d'environnement :
   ```powershell
   setx STRIPE_WEBHOOK_SECRET "whsec_..."
   ```

### Cartes de test Stripe

| Numero                 | Resultat                    |
|-------------------------|------------------------------|
| `4242 4242 4242 4242`   | Paiement accepte              |
| `4000 0000 0000 0002`   | Carte refusee                 |

Date d'expiration : n'importe quelle date future. CVC : n'importe quel 3 chiffres.

## Principaux endpoints de l'API

| Methode | URL                          | Description                              | Auth requise |
|---------|-------------------------------|--------------------------------------------|:---:|
| GET     | `/api/games`                  | Liste des jeux (filtre `?category=`)       | non |
| GET     | `/api/games/{id}`             | Detail d'un jeu                             | non |
| POST    | `/api/auth/register`          | Creer un compte                             | non |
| POST    | `/api/auth/login`             | Se connecter (retourne un JWT)              | non |
| GET     | `/api/auth/me`                | Utilisateur courant                         | oui |
| POST    | `/api/orders`                 | Creer une commande a partir du panier       | oui |
| GET     | `/api/orders/{id}`            | Consulter une commande (proprietaire/admin) | oui |
| POST    | `/api/payments/create-intent` | Initialiser le paiement Stripe d'une commande | oui |
| POST    | `/api/payments/webhook`       | Appele par Stripe (signature verifiee)      | non (signature) |

## Stack technique

- **Frontend** : Angular 19, composants standalone, signals, SCSS, Stripe.js
- **Backend** : Spring Boot 4.1, Java 21, Spring Security (JWT), Spring Data JPA, H2, Stripe Java SDK, Bean Validation, Lombok, Maven
