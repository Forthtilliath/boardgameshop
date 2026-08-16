import { Component, inject, signal } from '@angular/core';

import { User } from '../../../models/user.model';
import { AdminUserService } from '../../../services/admin-user.service';

@Component({
  selector: 'app-admin-users-list',
  imports: [],
  templateUrl: './admin-users-list.component.html',
  styleUrl: './admin-users-list.component.scss'
})
export class AdminUsersListComponent {
  private readonly adminUserService = inject(AdminUserService);

  readonly users = signal<User[]>([]);
  readonly loading = signal(true);

  constructor() {
    this.adminUserService.getUsers().subscribe((users) => {
      this.users.set(users);
      this.loading.set(false);
    });
  }
}
