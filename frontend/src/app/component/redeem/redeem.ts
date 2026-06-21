import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-redeem',
  templateUrl: './redeem.html',
  styleUrls: ['./redeem.css'],
  standalone: false
})
export class Redeem implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  accId = 0;

  ngOnInit(): void {
    this.accId = Number(this.route.snapshot.paramMap.get('id')) || 0;
    if (this.accId <= 0) { this.router.navigate(['/']); }
  }

  goDashboard() {
    this.router.navigate(['/dashboard', this.accId]);
  }
}
