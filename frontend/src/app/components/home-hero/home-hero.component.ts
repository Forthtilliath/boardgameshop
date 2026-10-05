import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Bandeau d'accueil, décoré de pièces de jeu (dé, meeple, carte, tuile, jeton). */
@Component({
  selector: 'app-home-hero',
  imports: [RouterLink],
  templateUrl: './home-hero.component.html',
  styleUrl: './home-hero.component.scss'
})
export class HomeHeroComponent {}
