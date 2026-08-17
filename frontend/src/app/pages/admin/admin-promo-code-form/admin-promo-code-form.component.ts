import type { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import type { AdminPromoCodeRequest } from '../../../models/promo-code.model';
import { AdminPromoCodeService } from '../../../services/admin-promo-code.service';

@Component({
  selector: 'app-admin-promo-code-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admin-promo-code-form.component.html',
  styleUrl: './admin-promo-code-form.component.scss'
})
export class AdminPromoCodeFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly adminPromoCodeService = inject(AdminPromoCodeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly promoCodeId = this.route.snapshot.paramMap.get('id');
  readonly isEditMode = this.promoCodeId !== null;

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    code: ['', Validators.required],
    discountPercent: [10, [Validators.required, Validators.min(1), Validators.max(100)]],
    active: [true],
    expiresAt: [''],
    maxUses: [null as number | null]
  });

  constructor() {
    if (this.isEditMode) {
      this.adminPromoCodeService.getPromoCodes().subscribe((promoCodes) => {
        const promoCode = promoCodes.find((p) => p.id === Number(this.promoCodeId));
        if (!promoCode) {
          return;
        }
        this.form.patchValue({
          code: promoCode.code,
          discountPercent: promoCode.discountPercent,
          active: promoCode.active,
          expiresAt: promoCode.expiresAt ? promoCode.expiresAt.slice(0, 10) : '',
          maxUses: promoCode.maxUses
        });
      });
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    const request: AdminPromoCodeRequest = {
      code: raw.code,
      discountPercent: raw.discountPercent,
      active: raw.active,
      expiresAt: raw.expiresAt ? new Date(raw.expiresAt + 'T23:59:59').toISOString() : null,
      maxUses: raw.maxUses
    };

    const save$ = this.isEditMode
      ? this.adminPromoCodeService.updatePromoCode(Number(this.promoCodeId), request)
      : this.adminPromoCodeService.createPromoCode(request);

    save$.subscribe({
      next: () => { void this.router.navigateByUrl('/admin/codes-promo'); },
      error: (err: HttpErrorResponse) => {
        const message = (err.error as { message?: string } | null)?.message;
        this.errorMessage.set(message ?? 'Enregistrement impossible.');
        this.submitting.set(false);
      }
    });
  }
}
