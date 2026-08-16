import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AdminGameRequest } from '../../../models/game.model';
import { Tag } from '../../../models/tag.model';
import { AdminGameService } from '../../../services/admin-game.service';
import { TagService } from '../../../services/tag.service';

@Component({
  selector: 'app-admin-game-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admin-game-form.component.html',
  styleUrl: './admin-game-form.component.scss'
})
export class AdminGameFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly adminGameService = inject(AdminGameService);
  private readonly tagService = inject(TagService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly gameId = this.route.snapshot.paramMap.get('id');
  readonly isEditMode = this.gameId !== null;

  readonly tags = signal<Tag[]>([]);
  readonly selectedTagIds = signal<Set<number>>(new Set());
  readonly newTagName = signal('');

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    price: [0, [Validators.required, Validators.min(0)]],
    category: [''],
    imageUrl: [''],
    publisher: [''],
    minPlayers: [null as number | null],
    maxPlayers: [null as number | null],
    durationMinutes: [null as number | null],
    stock: [0, [Validators.required, Validators.min(0)]],
    minAge: [null as number | null],
    releaseDate: [''],
    discountPercent: [null as number | null, [Validators.min(1), Validators.max(100)]],
    discountEndsAt: ['']
  });

  constructor() {
    this.tagService.getTags().subscribe((tags) => this.tags.set(tags));

    if (this.isEditMode) {
      this.adminGameService.getGames().subscribe((games) => {
        const game = games.find((g) => g.id === Number(this.gameId));
        if (!game) {
          return;
        }
        this.form.patchValue({
          name: game.name,
          description: game.description,
          price: game.price,
          category: game.category,
          imageUrl: game.imageUrl,
          publisher: game.publisher,
          minPlayers: game.minPlayers,
          maxPlayers: game.maxPlayers,
          durationMinutes: game.durationMinutes,
          stock: game.stock,
          minAge: game.minAge,
          releaseDate: game.releaseDate ?? '',
          discountPercent: game.discountPercent,
          discountEndsAt: game.discountEndsAt ? game.discountEndsAt.slice(0, 10) : ''
        });
        this.selectedTagIds.set(new Set(game.tags.map((t) => t.id)));
      });
    }
  }

  toggleTag(tagId: number): void {
    const current = new Set(this.selectedTagIds());
    if (current.has(tagId)) {
      current.delete(tagId);
    } else {
      current.add(tagId);
    }
    this.selectedTagIds.set(current);
  }

  createTag(): void {
    const name = this.newTagName().trim();
    if (!name) {
      return;
    }
    this.tagService.createTag(name).subscribe((tag) => {
      this.tags.update((tags) => [...tags, tag]);
      this.toggleTag(tag.id);
      this.newTagName.set('');
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    const request: AdminGameRequest = {
      name: raw.name,
      description: raw.description || null,
      price: raw.price,
      category: raw.category || null,
      imageUrl: raw.imageUrl || null,
      publisher: raw.publisher || null,
      minPlayers: raw.minPlayers,
      maxPlayers: raw.maxPlayers,
      durationMinutes: raw.durationMinutes,
      stock: raw.stock,
      minAge: raw.minAge,
      releaseDate: raw.releaseDate || null,
      discountPercent: raw.discountPercent,
      discountEndsAt: raw.discountEndsAt ? new Date(raw.discountEndsAt + 'T23:59:59').toISOString() : null,
      tagIds: [...this.selectedTagIds()]
    };

    const save$ = this.isEditMode
      ? this.adminGameService.updateGame(Number(this.gameId), request)
      : this.adminGameService.createGame(request);

    save$.subscribe({
      next: () => this.router.navigateByUrl('/admin/jeux'),
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Enregistrement impossible.');
        this.submitting.set(false);
      }
    });
  }
}
