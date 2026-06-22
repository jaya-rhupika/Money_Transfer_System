import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Accountholderservice } from '../../service/accountholderservice';
import { AuthService } from '../../service/auth';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login {
  service: Accountholderservice;

  username: string = '';
  password: string = '';
  isLoggedin = false;
  error: string = '';
  isSubmitting = false;
  data: any = {};

  constructor(private router: Router, private authService: AuthService, accountholderservice: Accountholderservice) {
    this.service = accountholderservice;
  }

  ngOnInit(): void {
    this.isLoggedin = this.authService.isUserLoggedin();
    if (this.isLoggedin) {
      const userId = this.authService.getLoggedinUser();
      this.router.navigate(['/dashboard', userId]);
    }
  }

  doLogin() {
    this.error = '';

    if (!this.username || !this.password) {
      this.error = 'Please enter your account number and password.';
      return;
    }

    if (this.isSubmitting) return;
    this.isSubmitting = true;

    this.authService.authenticate(this.username, this.password).subscribe({
      next: (data) => {
        this.data = data;
        this.isSubmitting = false;

        let userId: any = this.username;
        if (data) {
          if ((data as any).accountId) userId = (data as any).accountId;
          else if ((data as any).id) userId = (data as any).id;
        }
        this.router.navigate(['/dashboard', userId]);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.error =
          err?.error?.message ||
          err?.error?.finalMessage ||
          'Invalid account number or password. Please try again.';
      }
    });
  }

  goToRegister() {
    this.router.navigate(['/register']);
  }
}
