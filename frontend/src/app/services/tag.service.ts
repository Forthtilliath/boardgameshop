import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Tag } from '../models/tag.model';

@Injectable({ providedIn: 'root' })
export class TagService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/tags`;

  getTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.baseUrl);
  }

  createTag(name: string): Observable<Tag> {
    return this.http.post<Tag>(this.baseUrl, { name });
  }
}
