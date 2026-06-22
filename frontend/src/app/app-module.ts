import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Login } from './component/login/login';
import { Register } from './component/register/register';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Dashboard } from './component/dashboard/dashboard';
import { RouterLink, RouterModule, RouterOutlet } from '@angular/router';
import { Transfer } from './component/transfer/transfer';
import { History } from './component/history/history';
import { Rewards } from './component/rewards/rewards';
import { Redeem } from './component/redeem/redeem';
import { Profile } from './component/profile/profile';
import { CommonModule } from '@angular/common';
import { HttpinterceptorService } from './service/httpinterceptor';
import { HTTP_INTERCEPTORS, provideHttpClient, withFetch, withInterceptorsFromDi } from '@angular/common/http';
import { NgChartsModule } from 'ng2-charts';

@NgModule({
  declarations: [
    App,
    Login,
    Register,
    Dashboard,
    Transfer,
    History,
    Rewards,
    Redeem,
    Profile
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    RouterOutlet,
    RouterLink,
    ReactiveFormsModule,
    CommonModule,
    NgChartsModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpinterceptorService,
      multi: true
    },
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(
      withFetch(),
      withInterceptorsFromDi()
    ),
    provideClientHydration(withEventReplay())
  ],
  bootstrap: [App]
})
export class AppModule { }
