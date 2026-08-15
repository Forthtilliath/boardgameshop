# BGS - BoardGameShop

Site fictif de vente de jeux de societe, construit avec **Angular** (frontend) et **Java / Spring Boot** (backend).

## Structure du depot

```
boardgameshop/
├── frontend/   Application Angular (catalogue, fiche jeu, panier)
└── backend/    API REST Spring Boot (catalogue, commandes)
```

## Fonctionnalites (v1)

- Catalogue de jeux de societe avec filtre par categorie
- Fiche detaillee de chaque jeu
- Panier persistant (localStorage), sans compte utilisateur
- Passage de commande simule (pas de paiement reel), avec decrement du stock

## Prerequis

- Node.js 22+ et npm
- Java 21
- (Maven n'est pas requis : le wrapper `mvnw` / `mvnw.cmd` est inclus)

## Demarrer le backend

```bash
cd backend
./mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # Linux / macOS
```

L'API demarre sur `http://localhost:8080`. Une base H2 en memoire est peuplee automatiquement
avec un jeu de donnees de demonstration au demarrage. Console H2 disponible sur `/h2-console`.

## Demarrer le frontend

```bash
cd frontend
npm install
npm start
```

L'application est disponible sur `http://localhost:4200` et appelle l'API sur `http://localhost:8080/api`.

## Principaux endpoints de l'API

| Methode | URL                | Description                          |
|---------|---------------------|---------------------------------------|
| GET     | `/api/games`         | Liste des jeux (filtre `?category=`) |
| GET     | `/api/games/{id}`    | Detail d'un jeu                       |
| POST    | `/api/orders`        | Creer une commande a partir du panier |
| GET     | `/api/orders/{id}`   | Consulter une commande                |

## Stack technique

- **Frontend** : Angular 19, composants standalone, signals, SCSS
- **Backend** : Spring Boot 4.1, Java 21, Spring Data JPA, H2, Bean Validation, Lombok, Maven
