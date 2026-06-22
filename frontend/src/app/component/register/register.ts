import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.html',
  styleUrls: ['./register.css'],
})
export class Register {
  private router = inject(Router);
  private http = inject(HttpClient);
  private cd = inject(ChangeDetectorRef);

  name: string = '';
  password: string = '';
  confirmPassword: string = '';
  isSubmitting = false;
  error: string = '';

  showWelcomePopup = false;
  newAccountId: number = 0;

  doRegister() {
    this.error = '';

    if (!this.name || !this.password || !this.confirmPassword) {
      this.error = 'Please fill in all fields.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.error = 'Passwords do not match.';
      return;
    }

    if (this.isSubmitting) return;
    this.isSubmitting = true;

    const payload = { holderName: this.name, password: this.password };

    this.http.post<any>('/api/v1/accounts/register', payload).subscribe({
      next: (res) => {
        this.newAccountId = res.accountId || res.id || 0;
        this.isSubmitting = false;
        this.showWelcomePopup = true;
        this.cd.detectChanges();          // ← forces the view to update
      },
      error: (err) => {
        this.isSubmitting = false;
        this.error =
          err?.error?.message ||
          err?.error?.finalMessage ||
          'Registration failed. Please try again.';
        this.cd.detectChanges();
      }
    });
  }

  goToDashboard() {
    this.showWelcomePopup = false;
    this.router.navigate(['/dashboard', this.newAccountId]);
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}