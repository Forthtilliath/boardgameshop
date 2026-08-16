import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import type { Observable} from 'rxjs';
import { catchError, of, tap } from 'rxjs';

import { environment } from '../../environments/environment';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';
import type { User } from '../models/user.model';

const TOKEN_KEY = 'bgs-token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  private readonly currentUserSignal = signal<User | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(
      tap((response) => { this.applyAuth(response); })
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((response) => { this.applyAuth(response); })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.currentUserSignal.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /**
   * Recharge l'utilisateur courant à partir d'un token déjà stocké (au démarrage
   * de l'app). Appelé une seule fois via provideAppInitializer.
   */
  restoreSession(): Observable<unknown> {
    const token = this.getToken();
    if (!token) {
      return of(null);
    }

    return this.http.get<User>(`${this.baseUrl}/me`).pipe(
      tap((user) => { this.currentUserSignal.set(user); }),
      catchError(() => {
        this.logout();
        return of(null);
      })
    );
  }

  private applyAuth(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    this.currentUserSignal.set(response.user);
  }
}
