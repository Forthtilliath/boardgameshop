import type { ElementRef } from '@angular/core';
import { Component, input, output, viewChild } from '@angular/core';

import type { Game } from '../../models/game.model';
import { GameCardComponent } from '../game-card/game-card.component';

@Component({
  selector: 'app-game-slider',
  imports: [GameCardComponent],
  templateUrl: './game-slider.component.html',
  styleUrl: './game-slider.component.scss'
})
export class GameSliderComponent {
  readonly title = input.required<string>();
  readonly games = input.required<Game[]>();
  readonly addToCart = output<Game>();

  private readonly track = viewChild<ElementRef<HTMLDivElement>>('track');

  scroll(direction: -1 | 1): void {
    const element = this.track()?.nativeElement;
    if (!element) {
      return;
    }
    element.scrollBy({ left: direction * element.clientWidth * 0.8, behavior: 'smooth' });
  }
}
