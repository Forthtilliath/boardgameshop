import { HttpClient } from '@angular/common/http';
import { inject,Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type { Tag } from '../models/tag.model';

@Injectable({ providedIn: 'root' })
export class TagService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/tags`;

  getTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.baseUrl);
  }

  /** Liste publique, utilisée par les filtres du catalogue (pas besoin d'être admin). */
  getPublicTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(`${environment.apiUrl}/tags`);
  }

  createTag(name: string): Observable<Tag> {
    return this.http.post<Tag>(this.baseUrl, { name });
  }
}
