import { Component, ChangeDetectorRef, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Accountholderservice } from '../../service/accountholderservice';
import { AuthService } from '../../service/auth';
import { AccountHolderInterface } from '../account-holder-interface';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(Accountholderservice);
  private authService = inject(AuthService);
  private cd = inject(ChangeDetectorRef);

  user?: AccountHolderInterface;
  accId: number = 0;
  loading: boolean = true;
  passwordPopup = false;
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  passwordMessage = '';
  passwordError = '';
  savingPassword = false;

  ngOnInit(): void {
    this.accId = Number(this.route.snapshot.paramMap.get('id')) || 0;
    if (this.accId > 0) {
      this.loadUser();
    } else {
      this.router.navigate(['/']);
    }
  }

  loadUser() {
    this.service.getUserById(this.accId).subscribe({
      next: (data) => {
        this.user = data;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error loading user', err);
        this.loading = false;
        this.router.navigate(['/']);
      }
    });
  }

  goHome() {
    this.router.navigate(['/dashboard', this.accId]);
  }

  goToTransfer() {
    this.router.navigate(['/transfer', this.accId]);
  }

  goToHistory() {
    this.router.navigate(['/history', this.accId]);
  }

  openPasswordPopup() {
    this.passwordPopup = true;
    this.passwordError = '';
    this.passwordMessage = '';
  }

  closePasswordPopup() {
    this.passwordPopup = false;
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.passwordError = '';
    this.passwordMessage = '';
  }

  async changePassword() {
    this.passwordError = '';
    this.passwordMessage = '';

    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      this.passwordError = 'Please fill all password fields.';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.passwordError = 'New password and confirmation must match.';
      return;
    }

    this.savingPassword = true;
    this.authService.changePassword(this.currentPassword, this.newPassword, this.confirmPassword)
      .subscribe({
        next: (res: any) => {
          this.authService.logout();
          this.savingPassword = false;
          this.router.navigate(['/login']);
          this.cd.detectChanges();
        },
        error: (err) => {
          this.passwordError = err?.error || 'Could not update password.';
          this.savingPassword = false;
          this.cd.detectChanges();
        }
      });
  }
}

