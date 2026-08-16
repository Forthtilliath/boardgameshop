import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';

import type { AdminStats } from '../../../models/admin-stats.model';
import { AdminStatsService } from '../../../services/admin-stats.service';

@Component({
  selector: 'app-admin-stats',
  imports: [CurrencyPipe],
  templateUrl: './admin-stats.component.html',
  styleUrl: './admin-stats.component.scss'
})
export class AdminStatsComponent {
  private readonly adminStatsService = inject(AdminStatsService);

  readonly stats = signal<AdminStats | null>(null);

  readonly maxQuantity = computed(() => {
    const games = this.stats()?.topSellingGames ?? [];
    return games.reduce((max, g) => Math.max(max, g.quantitySold), 0);
  });

  constructor() {
    this.adminStatsService.getStats().subscribe((stats) => { this.stats.set(stats); });
  }

  barWidth(quantity: number): number {
    const max = this.maxQuantity();
    return max === 0 ? 0 : (quantity / max) * 100;
  }
}
