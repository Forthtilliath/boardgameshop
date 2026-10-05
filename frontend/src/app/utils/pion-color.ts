/** Couleurs de pions du thème (variables CSS --pion-* de src/styles/_tokens.scss). */
const PION_COLORS = ['red', 'orange', 'yellow', 'green', 'blue', 'purple', 'pink'] as const;

/**
 * Couleur de pion stable pour un identifiant donné : un même jeu garde la même
 * couleur partout (carte du catalogue, slider, fiche), quelle que soit sa
 * position dans la liste affichée.
 */
export function pionColor(seed: number): string {
  return `var(--pion-${PION_COLORS[Math.abs(seed) % PION_COLORS.length]})`;
}
