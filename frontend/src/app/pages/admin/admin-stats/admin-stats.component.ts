import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';

import { DailyBarChartComponent, type DailyChartPoint } from '../../../components/daily-bar-chart/daily-bar-chart.component';
import { RankedBarListComponent, type RankedBarItem } from '../../../components/ranked-bar-list/ranked-bar-list.component';
import type { AdminStats } from '../../../models/admin-stats.model';
import { orderStatusLabel } from '../../../models/order.model';
import { AdminStatsService } from '../../../services/admin-stats.service';

const NUMBER_FORMAT = new Intl.NumberFormat('fr-FR');
const CURRENCY_FORMAT = new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' });

@Component({
  selector: 'app-admin-stats',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, DailyBarChartComponent, RankedBarListComponent],
  templateUrl: './admin-stats.component.html',
  styleUrl: './admin-stats.component.scss'
})
export class AdminStatsComponent {
  private readonly adminStatsService = inject(AdminStatsService);

  readonly stats = signal<AdminStats | null>(null);

  constructor() {
    this.adminStatsService.getStats().subscribe((stats) => { this.stats.set(stats); });
  }

  // ------------------------------------------------------------- séries journalières

  readonly ordersByDayPoints = computed<DailyChartPoint[]>(() =>
    (this.stats()?.sales.ordersByDay ?? []).map((d) => ({ date: d.date, value: d.count }))
  );

  readonly revenueByDayPoints = computed<DailyChartPoint[]>(() =>
    (this.stats()?.sales.revenueByDay ?? []).map((d) => ({ date: d.date, value: d.revenue }))
  );

  readonly signupsByDayPoints = computed<DailyChartPoint[]>(() =>
    (this.stats()?.users.signupsByDay ?? []).map((d) => ({ date: d.date, value: d.count }))
  );

  // ------------------------------------------------------------------- ventes

  readonly topSellingItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.sales.topSellingGames ?? []).map((g) => ({
      id: g.gameId,
      label: g.gameName,
      value: g.quantitySold,
      formattedValue: `${NUMBER_FORMAT.format(g.quantitySold)} vendu(s)`,
      tooltip: `${g.gameName} : ${g.quantitySold} vendu(s), ${CURRENCY_FORMAT.format(g.revenue)}`
    }))
  );

  readonly ordersByStatusItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.sales.ordersByStatus ?? []).map((s) => ({
      id: s.status,
      label: orderStatusLabel(s.status),
      value: s.count,
      formattedValue: NUMBER_FORMAT.format(s.count)
    }))
  );

  readonly revenueByCategoryItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.sales.revenueByCategory ?? []).map((c) => ({
      id: c.category,
      label: c.category,
      value: c.revenue,
      formattedValue: CURRENCY_FORMAT.format(c.revenue)
    }))
  );

  readonly promoCodeItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.sales.promoCodeUsage ?? []).map((p) => ({
      id: p.code,
      label: p.code,
      value: p.usesCount,
      formattedValue: p.maxUses != null ? `${p.usesCount}/${p.maxUses}` : `${p.usesCount}`,
      tooltip: p.active ? 'Code actif' : 'Code inactif ou expiré'
    }))
  );

  // ----------------------------------------------------------------- catalogue

  readonly lowStockItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.catalog.lowStockGames ?? []).map((g) => ({
      id: g.gameId,
      label: g.gameName,
      value: g.stock,
      formattedValue: g.stock === 0 ? 'Rupture' : `${g.stock} en stock`
    }))
  );

  readonly topFavoriteItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.catalog.topFavoriteGames ?? []).map((g) => ({
      id: g.gameId,
      label: g.gameName,
      value: g.favoriteCount,
      formattedValue: `${NUMBER_FORMAT.format(g.favoriteCount)} favori(s)`
    }))
  );

  readonly restockDemandItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.catalog.restockDemand ?? []).map((g) => ({
      id: g.gameId,
      label: g.gameName,
      value: g.alertCount,
      formattedValue: `${NUMBER_FORMAT.format(g.alertCount)} alerte(s)`
    }))
  );

  // -------------------------------------------------------------------- avis

  readonly ratingDistributionItems = computed<RankedBarItem[]>(() =>
    [...(this.stats()?.reviews.ratingDistribution ?? [])]
      .sort((a, b) => b.stars - a.stars)
      .map((r) => ({
        id: r.stars,
        label: `${r.stars} ★`,
        value: r.count,
        formattedValue: `${NUMBER_FORMAT.format(r.count)} avis`
      }))
  );

  readonly topRatedItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.reviews.topRatedGames ?? []).map((g) => ({
      id: g.gameId,
      label: g.gameName,
      value: g.averageRating,
      formattedValue: `${g.averageRating.toFixed(1)} ★`,
      tooltip: `${g.reviewsCount} avis`
    }))
  );

  readonly worstRatedItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.reviews.worstRatedGames ?? []).map((g) => ({
      id: g.gameId,
      label: g.gameName,
      value: g.averageRating,
      formattedValue: `${g.averageRating.toFixed(1)} ★`,
      tooltip: `${g.reviewsCount} avis`
    }))
  );

  readonly mostReviewedItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.reviews.mostReviewedGames ?? []).map((g) => ({
      id: g.gameId,
      label: g.gameName,
      value: g.reviewsCount,
      formattedValue: `${NUMBER_FORMAT.format(g.reviewsCount)} avis`
    }))
  );

  // --------------------------------------------------------------- utilisateurs

  readonly topCustomerItems = computed<RankedBarItem[]>(() =>
    (this.stats()?.users.topCustomers ?? []).map((c) => ({
      id: c.userId,
      label: c.customerName,
      value: c.totalSpent,
      formattedValue: CURRENCY_FORMAT.format(c.totalSpent),
      tooltip: `${c.orderCount} commande(s)`
    }))
  );
}
