import { Component, computed, input } from '@angular/core';

export interface RankedBarItem {
  id: number | string;
  label: string;
  value: number;
  /** Valeur déjà formatée (nombre, devise, note...) à afficher à droite de la barre. */
  formattedValue: string;
  /** Texte affiché au survol de la barre (détail complémentaire). */
  tooltip?: string;
}

/**
 * Classement en barres horizontales (nom + barre proportionnelle + valeur) : la
 * forme réutilisée pour tous les "top N" du dashboard admin (ventes, favoris,
 * avis, clients...). Une seule série : pas de légende nécessaire, le titre de
 * la section au-dessus la nomme. Pas de dépendance de charting externe.
 */
@Component({
  selector: 'app-ranked-bar-list',
  imports: [],
  templateUrl: './ranked-bar-list.component.html',
  styleUrl: './ranked-bar-list.component.scss'
})
export class RankedBarListComponent {
  readonly items = input.required<RankedBarItem[]>();
  readonly emptyMessage = input('Pas encore de données.');
  readonly color = input('var(--color-primary)');

  readonly maxValue = computed(() => Math.max(1, ...this.items().map((item) => item.value)));

  barWidth(value: number): number {
    return (value / this.maxValue()) * 100;
  }
}
