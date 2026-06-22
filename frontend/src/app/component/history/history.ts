import { Component, ChangeDetectorRef, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Accountholderservice } from '../../service/accountholderservice';
import { TransactionLogInterface } from '../transaction-log-interface';

type FilterMode = 'all' | 'sent' | 'received';

@Component({
  selector: 'app-history',
  templateUrl: './history.html',
  styleUrls: ['./history.css'],
  standalone: false
})
export class History implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(Accountholderservice);
  private cd = inject(ChangeDetectorRef);

  accId = 0;
  transactions: TransactionLogInterface[] = [];
  loading = true;
  filterMode: FilterMode = 'all';
  searchQuery = '';

  ngOnInit(): void {
    this.accId = Number(this.route.snapshot.paramMap.get('id')) || 0;
    if (this.accId <= 0) {
      this.router.navigate(['/']);
      return;
    }

    this.service.getTransactionsById(this.accId).subscribe({
      next: (data) => {
        this.transactions = (data || []).slice().sort((a, b) =>
          (b.createdOn || '').localeCompare(a.createdOn || '')
        );
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Transaction API error:', err);
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  isCredit(t: TransactionLogInterface): boolean {
    return t.toAccountId === this.accId;
  }

  // Points are only earned on DEBIT (sent) transactions > 100
  getPoints(t: TransactionLogInterface): number {
    if (this.isCredit(t)) return 0;
    if ((t.points ?? 0) > 0) return t.points!;
    // fallback: calculate from amount in case backend missed it
    if (t.amount > 100 && (t.status || '').toUpperCase() === 'SUCCESS') {
      return Math.floor(t.amount / 100);
    }
    return 0;
  }

  private searchMatches(t: TransactionLogInterface): boolean {
    const query = this.searchQuery.toLowerCase().trim();
    if (!query) return true;
    const amount = t.amount.toString();
    const fromId = t.fromAccountId.toString();
    const toId = t.toAccountId.toString();
    return amount.includes(query) || fromId.includes(query) || toId.includes(query);
  }

  get filteredTransactions(): TransactionLogInterface[] {
    let result = this.transactions;
    if (this.filterMode === 'sent') {
      result = result.filter(t => !this.isCredit(t));
    } else if (this.filterMode === 'received') {
      result = result.filter(t => this.isCredit(t));
    }
    return result.filter(t => this.searchMatches(t));
  }

  setFilter(mode: FilterMode) {
    this.filterMode = mode;
  }

  statusText(t: TransactionLogInterface): string {
    return (t.status || '').toUpperCase() === 'SUCCESS' ? 'Success' : 'Failed';
  }

  goHome() { this.router.navigate(['/dashboard', this.accId]); }
  goToTransfer() { this.router.navigate(['/transfer', this.accId]); }
  goToHistory() { this.router.navigate(['/history', this.accId]); }
}
