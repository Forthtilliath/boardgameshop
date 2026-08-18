import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../environments/environment';
import type { PageResponse } from '../models/page-response.model';
import { GameService } from './game.service';

const baseUrl = `${environment.apiUrl}/games`;

const emptyPage: PageResponse<unknown> = { content: [], page: 0, size: 24, totalElements: 0, totalPages: 0 };

describe('GameService', () => {
  let service: GameService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(GameService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('requests the catalog with pagination params by default', () => {
    service.getGames().subscribe();

    const req = httpMock.expectOne((r) => r.url === baseUrl);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('24');
    req.flush(emptyPage);
  });

  it('forwards filter fields as query params', () => {
    service.getGames({ category: 'Stratégie', search: 'catan', sort: 'price_asc', tags: [1, 2] }, 1, 12).subscribe();

    const req = httpMock.expectOne((r) => r.url === baseUrl);
    expect(req.request.params.get('category')).toBe('Stratégie');
    expect(req.request.params.get('search')).toBe('catan');
    expect(req.request.params.get('sort')).toBe('price_asc');
    expect(req.request.params.getAll('tags')).toEqual(['1', '2']);
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('12');
    req.flush(emptyPage);
  });

  it('omits absent filter fields from the query', () => {
    service.getGames({}).subscribe();

    const req = httpMock.expectOne((r) => r.url === baseUrl);
    expect(req.request.params.has('category')).toBeFalse();
    expect(req.request.params.has('search')).toBeFalse();
    req.flush(emptyPage);
  });

  it('fetches distinct categories', () => {
    service.getCategories().subscribe((categories) => {
      expect(categories).toEqual(['Famille', 'Stratégie']);
    });

    httpMock.expectOne(`${baseUrl}/categories`).flush(['Famille', 'Stratégie']);
  });

  it('fetches a single game by id', () => {
    service.getGame(42).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/42`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('fetches related games for a given id', () => {
    service.getRelatedGames(42).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/42/related`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
