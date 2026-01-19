import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginHistoryComponent } from './login-history.component';

const routes: Routes = [
  {
    path: '',
    component: LoginHistoryComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LoginHistoryRoutingModule { }

