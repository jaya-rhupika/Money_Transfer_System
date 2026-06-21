import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Accountholderservice } from '../../service/accountholderservice';
import { TransactionLogInterface } from '../transaction-log-interface';

@Component({
  selector: 'app-rewards',
  templateUrl: './rewards.html',
  styleUrls: ['./rewards.css'],
  standalone: false
})
export class Rewards implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(Accountholderservice);
  private cd = inject(ChangeDetectorRef);

  accId = 0;
  loading = true;
  rewardTransactions: TransactionLogInterface[] = [];
  totalPoints = 0;
  showPopup = false;

  ngOnInit(): void {
    this.accId = Number(this.route.snapshot.paramMap.get('id')) || 0;
    if (this.accId <= 0) {
      this.router.navigate(['/']);
      return;
    }

    this.service.getTransactionsById(this.accId).subscribe({
      next: (data) => {
        const all = (data || []).slice();
        this.rewardTransactions = all.filter(t => t.fromAccountId === this.accId && (t.points || 0) > 0);
        this.totalPoints = this.rewardTransactions.reduce((s, r) => s + (r.points || 0), 0);
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }

  showInfo() {
    this.showPopup = true;
  }

  closeInfo() {
    this.showPopup = false;
  }

  goRedeem() {
    this.router.navigate(['/redeem', this.accId]);
  }

  goHome() {
    this.router.navigate(['/dashboard', this.accId]);
  }

  goToTransfer() {
    this.router.navigate(['/transfer', this.accId]);
  }

  goToRewards() {
    this.router.navigate(['/rewards', this.accId]);
  }

  goToHistory() {
    this.router.navigate(['/history', this.accId]);
  }
}
