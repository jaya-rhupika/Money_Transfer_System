import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './component/login/login';
import { Register } from './component/register/register';
import { Dashboard } from './component/dashboard/dashboard';
import { Transfer } from './component/transfer/transfer';
import { Profile } from './component/profile/profile';
import { History } from './component/history/history';
import { Rewards } from './component/rewards/rewards';
import { Redeem } from './component/redeem/redeem';

const routes: Routes = [
  { path: 'login',          component: Login },
  { path: 'register',       component: Register },
  { path: 'dashboard/:id',  component: Dashboard },
  { path: 'transfer/:id',   component: Transfer },
  { path: 'history/:id',    component: History },
  { path: 'rewards/:id',    component: Rewards },
  { path: 'redeem/:id',     component: Redeem },
  { path: 'profile/:id',    component: Profile },
  { path: '',               redirectTo: 'login', pathMatch: 'full' },
  { path: '**',             redirectTo: 'login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
