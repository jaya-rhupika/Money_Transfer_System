import { Component, ChangeDetectorRef, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Accountholderservice } from '../../service/accountholderservice';
import { TransferService } from '../../transfer-service';
import { AccountHolderInterface } from '../account-holder-interface';
import { TransferRequestDto } from '../../models/transferModel';
import { v4 as uuidv4 } from 'uuid';

@Component({
  selector: 'app-transfer',
  standalone: false,
  templateUrl: './transfer.html',
  styleUrl: './transfer.css',
})
export class Transfer implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  service = inject(Accountholderservice);
  transferService = inject(TransferService);
  private cd = inject(ChangeDetectorRef);

  user?: AccountHolderInterface;
  accId: number = 0;

  recipientId: number = 0;
  transferAmount: number = 0;

  showSuccessModal: boolean = false;
  showErrorModal: boolean = false;
  transactionId: string = '';
  errorMessage: string = '';
  errorCode: string = '';
  isSubmitting: boolean = false;

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
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error loading user', err);
        this.router.navigate(['/']);
      }
    });
  }

  submitTransfer() {
    if (this.isSubmitting) return;
    this.isSubmitting = true;

    const request: TransferRequestDto = {
      fromAccountId: this.accId,
      toAccountId: this.recipientId,
      amount: this.transferAmount,
      idempotencyKey: uuidv4()
    };

    this.transferService.executeTransfer(request).subscribe({
      next: (response) => {
        this.transactionId = response.id;
        this.showSuccessModal = true;
        this.isSubmitting = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        // The backend ErrorResponseDto has fields: errorCode, errorMessage
        // (NOT "message" or "finalMessage")
        const body = err.error;
        this.errorCode    = body?.errorCode    || '';
        this.errorMessage = body?.errorMessage || body?.message || body?.finalMessage || 'An unexpected error occurred.';

        this.showErrorModal = true;
        this.isSubmitting = false;
        this.cd.detectChanges();
      }
    });
  }

  closeModalAndReturn() {
    this.showSuccessModal = false;
    this.router.navigate(['/dashboard', this.accId]);
  }

  closeErrorModal() {
    this.showErrorModal = false;
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
}
