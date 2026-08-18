import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'bgs-theme';

/**
 * Bascule clair/sombre. Tant que l'utilisateur n'a jamais cliqué sur le bouton du
 * header, aucun attribut n'est posé sur <html> : le thème suit `prefers-color-scheme`
 * nativement en CSS (voir styles.scss). Le premier clic fige un choix explicite,
 * mémorisé en localStorage.
 *
 * `localStorage`/`window.matchMedia` n'existent pas côté serveur (SSR) : tout accès
 * est gardé par `isPlatformBrowser`, avec repli sur "light" pendant le rendu serveur
 * (le thème réel s'applique ensuite côté client après hydratation).
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

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
    if (this.isBrowser) {
      localStorage.setItem(STORAGE_KEY, next);
    }
    this.apply(next);
  }

  private apply(theme: Theme | null): void {
    if (!this.isBrowser) {
      return;
    }
    if (theme) {
      document.documentElement.setAttribute('data-theme', theme);
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }

  private loadStoredTheme(): Theme | null {
    if (!this.isBrowser) {
      return null;
    }
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored === 'light' || stored === 'dark' ? stored : null;
  }

  private systemPreference(): Theme {
    if (!this.isBrowser) {
      return 'light';
    }
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
}
