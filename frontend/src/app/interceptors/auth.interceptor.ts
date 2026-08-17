import type { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

/** Endpoints d'auth eux-mêmes : jamais de tentative de refresh dessus (boucle infinie sinon). */
function isAuthEndpoint(url: string): boolean {
  return /\/auth\/(login|register|refresh|logout)$/.test(url);
}

function readCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
  return match ? decodeURIComponent(match[1]) : null;
}

/**
 * Le token n'est plus géré ici (cookie HttpOnly posé par le backend, invisible en
 * JS) : on se contente de faire circuler les cookies (`withCredentials`) et de
 * tenter un refresh silencieux sur un 401 avant de délonnecter l'utilisateur.
 *
 * Le header CSRF (X-XSRF-TOKEN) est ajouté ici à la main plutôt que via
 * `withXsrfConfiguration` : le support XSRF intégré d'Angular n'ajoute JAMAIS ce
 * header sur une URL absolue (http://...), précisément le cas de `environment.apiUrl`
 * en dev (backend sur un port différent) — protection anti-fuite d'Angular qui, ici,
 * désactiverait silencieusement le CSRF en dev. Sans risque : localhost:4200 et
 * localhost:8080 sont same-site, le cookie XSRF-TOKEN n'est de toute façon lisible
 * que par du JS tournant sur ce même site.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isUnsafeMethod = req.method !== 'GET' && req.method !== 'HEAD';
  const xsrfToken = isUnsafeMethod ? readCookie('XSRF-TOKEN') : null;

  const credentialedReq = req.clone({
    withCredentials: true,
    setHeaders: xsrfToken ? { 'X-XSRF-TOKEN': xsrfToken } : {}
  });

  return next(credentialedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const canRetryWithRefresh =
        error.status === 401 && authService.isAuthenticated() && !isAuthEndpoint(req.url);

      if (!canRetryWithRefresh) {
        if (error.status === 401 && authService.isAuthenticated()) {
          authService.logout();
          void router.navigate(['/connexion']);
        }
        return throwError(() => error);
      }

      return authService.refresh().pipe(
        switchMap(() => next(credentialedReq)),
        catchError((refreshError) => {
          authService.logout();
          void router.navigate(['/connexion']);
          return throwError(() => refreshError);
        })
      );
    })
  );
};
