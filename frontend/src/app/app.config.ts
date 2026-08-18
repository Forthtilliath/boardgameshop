import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import localeFr from '@angular/common/locales/fr';
import type { ApplicationConfig} from '@angular/core';
import { inject, LOCALE_ID, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { authInterceptor } from './interceptors/auth.interceptor';
import { AuthService } from './services/auth.service';
import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

registerLocaleData(localeFr);

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: LOCALE_ID, useValue: 'fr-FR' },
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    // Le header CSRF (X-XSRF-TOKEN) est ajoute a la main dans authInterceptor plutot
    // que via withXsrfConfiguration : voir le commentaire de authInterceptor pour le
    // pourquoi (le support XSRF integre d'Angular n'agit pas sur les URLs absolues).
    // withFetch() : recommande par Angular pour le SSR (meilleure compatibilite/perf
    // que l'emulation XHR cote serveur).
    provideHttpClient(withInterceptors([authInterceptor]), withFetch()),
    provideAppInitializer(() => {
      const authService = inject(AuthService);
      return authService.restoreSession();
    }), provideClientHydration(withEventReplay())
  ]
};
