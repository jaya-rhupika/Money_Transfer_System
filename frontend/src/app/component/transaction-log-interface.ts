export interface TransactionLogInterface {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  status: 'SUCCESS' | 'FAILED' | string;
  failureReason: string;
  idempotencyKey: string;
  createdOn: string; 
  points?: number;
}