import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import type { AdminPromoCodeResponse } from '../../../models/promo-code.model';
import { AdminPromoCodeService } from '../../../services/admin-promo-code.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-admin-promo-codes-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './admin-promo-codes-list.component.html',
  styleUrl: './admin-promo-codes-list.component.scss'
})
export class AdminPromoCodesListComponent {
  private readonly adminPromoCodeService = inject(AdminPromoCodeService);
  private readonly toastService = inject(ToastService);

  readonly promoCodes = signal<AdminPromoCodeResponse[]>([]);
  readonly loading = signal(true);

  constructor() {
    this.loadPromoCodes();
  }

  private loadPromoCodes(): void {
    this.loading.set(true);
    this.adminPromoCodeService.getPromoCodes().subscribe((promoCodes) => {
      this.promoCodes.set(promoCodes);
      this.loading.set(false);
    });
  }

  deletePromoCode(promoCode: AdminPromoCodeResponse): void {
    if (!confirm(`Supprimer le code "${promoCode.code}" ? Cette action est irréversible.`)) {
      return;
    }
    this.adminPromoCodeService.deletePromoCode(promoCode.id).subscribe(() => {
      this.loadPromoCodes();
      this.toastService.success(`Code "${promoCode.code}" supprimé`);
    });
  }
}
