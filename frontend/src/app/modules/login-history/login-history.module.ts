import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoginHistoryRoutingModule } from './login-history-routing.module';
import { LoginHistoryComponent } from './login-history.component';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';

@NgModule({
  declarations: [
    LoginHistoryComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    LoginHistoryRoutingModule,
    SvgIconComponent
  ]
})
export class LoginHistoryModule { }

