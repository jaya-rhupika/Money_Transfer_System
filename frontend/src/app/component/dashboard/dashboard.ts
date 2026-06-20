import { ChangeDetectorRef, Component, OnInit, ViewChild, ElementRef, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Accountholderservice } from '../../service/accountholderservice';
import { AuthService } from '../../service/auth';
import { AccountHolderInterface } from '../account-holder-interface';
import { Chart } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone:false
})
export class Dashboard implements OnInit {

  @ViewChild('transactionChart') transactionChart?: ElementRef<HTMLCanvasElement>;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(Accountholderservice);
  private authService = inject(AuthService);
  private cd=inject(ChangeDetectorRef);

  user?: AccountHolderInterface;
  accId: number = 0;
  
  chart: Chart | null = null;
  statsLoading = true;
  stats = {
    totalSent: 0,
    totalReceived: 0,
    sentCount: 0,
    receivedCount: 0
  };

  ngOnInit(): void {
    // Get ID from route parameter
    this.accId = Number(this.route.snapshot.paramMap.get('id')) || 0;

    if (this.accId > 0) {
      this.loadUser();
      this.loadTransactionStats();
    } else {
      this.router.navigate(['/']); // invalid access
    }
  }

  ngAfterViewInit() {
    if (this.transactionChart && this.chart === null && this.stats.sentCount + this.stats.receivedCount > 0) {
      this.initializeChart();
    }
  }

  loadUser() {
    this.service.getUserById(this.accId).subscribe({
      next: (data) => {
        this.user = data;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error loading user', err);
        this.router.navigate(['/']);
      }
    });
  }

  loadTransactionStats() {
    this.service.getTransactionsById(this.accId).subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.calculateStats(data);
        }
        this.statsLoading = false;
        this.cd.detectChanges();
        
        // Initialize chart after view is ready
        setTimeout(() => {
          if (this.transactionChart && this.chart === null) {
            this.initializeChart();
          }
        }, 100);
      },
      error: (err) => {
        console.error('Error loading transactions', err);
        this.statsLoading = false;
        this.cd.detectChanges();
      }
    });
  }

  calculateStats(transactions: any[]) {
    this.stats.sentCount = 0;
    this.stats.receivedCount = 0;
    this.stats.totalSent = 0;
    this.stats.totalReceived = 0;

    transactions.forEach(t => {
      if (t.status === 'SUCCESS' || t.status === 'success') {
        if (t.toAccountId === this.accId) {
          this.stats.receivedCount++;
          this.stats.totalReceived += t.amount;
        } else if (t.fromAccountId === this.accId) {
          this.stats.sentCount++;
          this.stats.totalSent += t.amount;
        }
      }
    });
  }

  initializeChart() {
    if (!this.transactionChart) return;

    const ctx = this.transactionChart.nativeElement.getContext('2d');
    if (!ctx) return;

    if (this.chart) {
      this.chart.destroy();
    }

    const total = this.stats.sentCount + this.stats.receivedCount;

    this.chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Sent', 'Received'],
        datasets: [
          {
            data: [this.stats.sentCount, this.stats.receivedCount],
            backgroundColor: ['#d32f2f', '#2e7d32'],
            borderColor: ['#ffffff', '#ffffff'],
            borderWidth: 3
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            position: 'bottom'
          },
          tooltip: {
            callbacks: {
              label: (context: any) => {
                const label = context.label || '';
                const value = context.parsed || 0;
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
                return `${label}: ${value} (${percentage}%)`;
              }
            }
          }
        }
      }
    });
  }

  // Navigation Methods
  goHome() {
    this.router.navigate(['/dashboard', this.accId]);
    this.cd.detectChanges();
  }

  goToTransfer() {
    this.router.navigate(['/transfer', this.accId]);
    this.cd.detectChanges();
  }

  goToHistory() {
    this.router.navigate(['/history', this.accId]);
    this.cd.detectChanges();
  }

  goToProfile() {
    this.router.navigate(['/profile', this.accId]);
    this.cd.detectChanges();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
    this.cd.detectChanges();
  }
  
}
