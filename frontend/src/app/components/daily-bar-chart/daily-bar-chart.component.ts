import { Component, computed, input, signal } from '@angular/core';

export interface DailyChartPoint {
  /** Date ISO (yyyy-MM-dd). */
  date: string;
  value: number;
}

/**
 * Graphe en barres verticales pour une série temporelle journalière (une seule
 * série : pas de légende nécessaire, le titre au-dessus du composant la nomme).
 * Survol = tooltip par barre. Pas de dépendance de charting externe : rendu en
 * HTML/CSS pur (flex + hauteurs en %), cohérent avec le style déjà utilisé pour
 * le "Top des ventes" du dashboard admin.
 */
@Component({
  selector: 'app-daily-bar-chart',
  imports: [],
  templateUrl: './daily-bar-chart.component.html',
  styleUrl: './daily-bar-chart.component.scss'
})
export class DailyBarChartComponent {
  readonly data = input.required<DailyChartPoint[]>();
  readonly color = input('var(--color-primary)');
  readonly valueFormat = input<'number' | 'currency'>('number');

  readonly hoveredIndex = signal<number | null>(null);

  readonly maxValue = computed(() => Math.max(1, ...this.data().map((d) => d.value)));

  barHeightPercent(value: number): number {
    return (value / this.maxValue()) * 100;
  }

  formatValue(value: number): string {
    return this.valueFormat() === 'currency'
      ? new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(value)
      : new Intl.NumberFormat('fr-FR').format(value);
  }

  formatDate(iso: string, withYear = false): string {
    const date = new Date(iso + 'T00:00:00');
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: withYear ? 'numeric' : undefined
    }).format(date);
  }
}
