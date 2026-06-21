export interface AccountHolderInterface {
  id: number,
  holderName: string,
  balance: number,
  status: string,
  version: number,
  lastUpdated: Date,
  active: boolean
  totalRewardPoints?: number
}
