import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import type { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { GameSliderComponent } from '../../components/game-slider/game-slider.component';
import type { Game } from '../../models/game.model';
import type { Review } from '../../models/review.model';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { FavoriteService } from '../../services/favorite.service';
import { GameService } from '../../services/game.service';
import { ReviewService } from '../../services/review.service';

@Component({
  selector: 'app-game-detail',
  imports: [RouterLink, CurrencyPipe, DatePipe, DecimalPipe, ReactiveFormsModule, GameSliderComponent],
  templateUrl: './game-detail.component.html',
  styleUrl: './game-detail.component.scss'
})
export class GameDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly gameService = inject(GameService);
  private readonly cartService = inject(CartService);
  private readonly favoriteService = inject(FavoriteService);
  private readonly authService = inject(AuthService);
  private readonly reviewService = inject(ReviewService);
  private readonly fb = inject(FormBuilder);

  private readonly gameId = Number(this.route.snapshot.paramMap.get('id'));

  readonly game = signal<Game | null>(null);
  readonly notFound = signal(false);
  readonly quantity = signal(1);
  readonly added = signal(false);
  readonly isAuthenticated = this.authService.isAuthenticated;

  readonly reviews = signal<Review[]>([]);
  readonly canReview = signal(false);
  readonly reviewSubmitting = signal(false);
  readonly reviewError = signal<string | null>(null);
  readonly ratingScale = [1, 2, 3, 4, 5];

  readonly relatedGames = signal<Game[]>([]);

  readonly reviewForm = this.fb.nonNullable.group({
    rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
    comment: ['']
  });

  constructor() {
    this.gameService.getGame(this.gameId).subscribe({
      next: (game) => { this.game.set(game); },
      error: () => { this.notFound.set(true); }
    });

    this.reviewService.getReviews(this.gameId).subscribe((reviews) => { this.reviews.set(reviews); });

    if (this.isAuthenticated()) {
      this.reviewService.canReview(this.gameId).subscribe((canReview) => { this.canReview.set(canReview); });
    }

    this.gameService.getRelatedGames(this.gameId).subscribe((games) => { this.relatedGames.set(games); });
  }

  addRelatedToCart(game: Game): void {
    this.cartService.addToCart(game);
  }

  submitReview(): void {
    if (this.reviewForm.invalid) {
      return;
    }

    this.reviewSubmitting.set(true);
    this.reviewError.set(null);

    const raw = this.reviewForm.getRawValue();
    this.reviewService.createReview(this.gameId, { rating: raw.rating, comment: raw.comment || null }).subscribe({
      next: (review) => {
        this.reviews.update((reviews) => [review, ...reviews]);
        this.canReview.set(false);
        this.reviewSubmitting.set(false);
        this.reviewForm.reset({ rating: 5, comment: '' });
      },
      error: (err: HttpErrorResponse) => {
        const message = (err.error as { message?: string } | null)?.message;
        this.reviewError.set(message ?? "Impossible d'enregistrer votre avis.");
        this.reviewSubmitting.set(false);
      }
    });
  }

  decrement(): void {
    this.quantity.update((q) => Math.max(1, q - 1));
  }

  increment(): void {
    const game = this.game();
    this.quantity.update((q) => (game ? Math.min(game.stock, q + 1) : q + 1));
  }

  addToCart(): void {
    const game = this.game();
    if (!game) {
      return;
    }
    this.cartService.addToCart(game, this.quantity());
    this.added.set(true);
    setTimeout(() => { this.added.set(false); }, 2000);
  }

  isFavorite(): boolean {
    const game = this.game();
    return game !== null && this.favoriteService.isFavorite(game.id);
  }

  toggleFavorite(): void {
    const game = this.game();
    if (game) {
      this.favoriteService.toggle(game.id);
    }
  }
}
