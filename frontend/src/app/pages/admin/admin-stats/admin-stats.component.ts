import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';

import { DailyBarChartComponent, type DailyChartPoint } from '../../../components/daily-bar-chart/daily-bar-chart.component';
import type { AdminStats } from '../../../models/admin-stats.model';
import { AdminStatsService } from '../../../services/admin-stats.service';

@Component({
  selector: 'app-admin-stats',
  imports: [CurrencyPipe, DatePipe, DailyBarChartComponent],
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

  /** Adapte les DTO back (count/revenue) au point générique {date, value} attendu par le graphe. */
  readonly ordersByDayPoints = computed<DailyChartPoint[]>(() =>
    (this.stats()?.ordersByDay ?? []).map((d) => ({ date: d.date, value: d.count }))
  );

  readonly revenueByDayPoints = computed<DailyChartPoint[]>(() =>
    (this.stats()?.revenueByDay ?? []).map((d) => ({ date: d.date, value: d.revenue }))
  );

  constructor() {
    this.adminStatsService.getStats().subscribe((stats) => { this.stats.set(stats); });
  }

  barWidth(quantity: number): number {
    const max = this.maxQuantity();
    return max === 0 ? 0 : (quantity / max) * 100;
  }
}
