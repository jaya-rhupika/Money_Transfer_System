import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './component/login/login';
import { Dashboard } from './component/dashboard/dashboard';
import { Transfer } from './component/transfer/transfer';

import { Profile } from './component/profile/profile';
import { History } from './component/history/history';
import { Rewards } from './component/rewards/rewards';
import { Redeem } from './component/redeem/redeem';

const routes: Routes = [
  {
    path:"login",
    component:Login
  },
  { 
    path: 'dashboard/:id', 
    component: Dashboard 
  },
  // Transfer
  { path: 'transfer/:id', component: Transfer },

  // History
  { path: 'history/:id', component: History },

  // Rewards
  { path: 'rewards/:id', component: Rewards },

  // Redeem (placeholder)
  { path: 'redeem/:id', component: Redeem },

  // Profile
  { path: 'profile/:id', component: Profile },

  // Default route - redirect to login
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  
  // Wildcard - redirect to login for any unmatched route
  { path: '**', redirectTo: 'login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
