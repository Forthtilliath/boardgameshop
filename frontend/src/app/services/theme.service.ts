import { Injectable, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'bgs-theme';

/**
 * Bascule clair/sombre. Tant que l'utilisateur n'a jamais cliqué sur le bouton du
 * header, aucun attribut n'est posé sur <html> : le thème suit `prefers-color-scheme`
 * nativement en CSS (voir styles.scss). Le premier clic fige un choix explicite,
 * mémorisé en localStorage.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly explicitThemeSignal = signal<Theme | null>(this.loadStoredTheme());
  private readonly themeSignal = signal<Theme>(this.explicitThemeSignal() ?? this.systemPreference());

  /** Thème effectivement appliqué (choix explicite, ou préférence système sinon). */
  readonly theme = this.themeSignal.asReadonly();

  constructor() {
    this.apply(this.explicitThemeSignal());
  }

  toggle(): void {
    const next: Theme = this.themeSignal() === 'dark' ? 'light' : 'dark';
    this.explicitThemeSignal.set(next);
    this.themeSignal.set(next);
    localStorage.setItem(STORAGE_KEY, next);
    this.apply(next);
  }

  private apply(theme: Theme | null): void {
    if (theme) {
      document.documentElement.setAttribute('data-theme', theme);
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }

  private loadStoredTheme(): Theme | null {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored === 'light' || stored === 'dark' ? stored : null;
  }

  private systemPreference(): Theme {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
}
