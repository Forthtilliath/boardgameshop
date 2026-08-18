import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../environments/environment';
import type { User } from '../models/user.model';
import { AuthService } from './auth.service';

const baseUrl = `${environment.apiUrl}/auth`;

const user: User = {
  id: 1,
  email: 'user@bgs.fr',
  firstName: 'Jean',
  lastName: 'Dupont',
  role: 'USER'
};

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('starts unauthenticated', () => {
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentUser()).toBeNull();
  });

  it('sets the current user on successful login', () => {
    service.login({ email: user.email, password: 'secret' }).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/login`);
    expect(req.request.method).toBe('POST');
    req.flush(user);

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.currentUser()).toEqual(user);
  });

  it('exposes isAdmin based on the current user role', () => {
    service.login({ email: user.email, password: 'secret' }).subscribe();
    httpMock.expectOne(`${baseUrl}/login`).flush({ ...user, role: 'ADMIN' });

    expect(service.isAdmin()).toBeTrue();
  });

  it('clears the current user on logout', () => {
    service.login({ email: user.email, password: 'secret' }).subscribe();
    httpMock.expectOne(`${baseUrl}/login`).flush(user);

    service.logout();
    httpMock.expectOne(`${baseUrl}/logout`).flush(null);

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentUser()).toBeNull();
  });

  it('restoreSession sets the user when the session cookie is valid', () => {
    service.restoreSession().subscribe();
    httpMock.expectOne(`${baseUrl}/me`).flush(user);

    expect(service.currentUser()).toEqual(user);
  });

  it('restoreSession leaves the user unauthenticated on a 401', () => {
    service.restoreSession().subscribe();
    httpMock.expectOne(`${baseUrl}/me`).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(service.isAuthenticated()).toBeFalse();
  });

  it('dedupes concurrent refresh calls into a single request', () => {
    let completions = 0;
    service.refresh().subscribe({ complete: () => completions++ });
    service.refresh().subscribe({ complete: () => completions++ });

    httpMock.expectOne(`${baseUrl}/refresh`).flush(null);

    expect(completions).toBe(2);
  });
});
