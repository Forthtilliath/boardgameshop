import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import type { Observable} from 'rxjs';
import { catchError, of, shareReplay, tap, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import type { LoginRequest, RegisterRequest } from '../models/auth.model';
import type { User } from '../models/user.model';
import { ToastService } from './toast.service';

/**
 * Le JWT n'est plus geré côté client : il est posé par le backend dans un cookie
 * HttpOnly (access + refresh), invisible en JS (immunisé contre le vol par XSS).
 * Ce service ne fait plus que suivre l'utilisateur courant en mémoire (signal) ;
 * `withCredentials` (voir authInterceptor) fait circuler les cookies automatiquement.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly toastService = inject(ToastService);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  private readonly currentUserSignal = signal<User | null>(null);
  /** Dédupe les refresh concurrents (plusieurs 401 en rafale ne déclenchent qu'un seul appel). */
  private refreshInProgress$: Observable<unknown> | null = null;

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');

  register(request: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/register`, request).pipe(
      tap((user) => {
        this.currentUserSignal.set(user);
        this.toastService.success(`Bienvenue, ${user.firstName} !`);
      })
    );
  }

  login(request: LoginRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/login`, request).pipe(
      tap((user) => {
        this.currentUserSignal.set(user);
        this.toastService.success(`Connecté en tant que ${user.firstName}`);
      })
    );
  }

  logout(): void {
    this.http.post(`${this.baseUrl}/logout`, {}).subscribe();
    this.currentUserSignal.set(null);
    this.toastService.info('Déconnecté');
  }

  /**
   * Renouvelle l'access token via le refresh token (cookie), utilisé par authInterceptor
   * sur un 401. Les appels concurrents partagent le même Observable (shareReplay) plutôt
   * que de déclencher un refresh par requête en échec.
   */
  refresh(): Observable<unknown> {
    if (!this.refreshInProgress$) {
      this.refreshInProgress$ = this.http.post(`${this.baseUrl}/refresh`, {}).pipe(
        tap({ complete: () => { this.refreshInProgress$ = null; } }),
        catchError((err) => {
          this.refreshInProgress$ = null;
          return throwError(() => err);
        }),
        shareReplay(1)
      );
    }
    return this.refreshInProgress$;
  }

  /**
   * Recharge l'utilisateur courant depuis le backend (le cookie de session, s'il
   * existe, est invisible en JS donc on tente toujours l'appel). Appelé une seule
   * fois via provideAppInitializer.
   */
  restoreSession(): Observable<unknown> {
    return this.http.get<User>(`${this.baseUrl}/me`).pipe(
      tap((user) => { this.currentUserSignal.set(user); }),
      catchError(() => {
        this.currentUserSignal.set(null);
        return of(null);
      })
    );
  }
}
