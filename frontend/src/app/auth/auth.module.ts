import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { LoginComponent } from './login/login.component';
import { UnauthorizedComponent } from './unauthorized/unauthorized.component';
import { AuthRoutingModule } from './auth-routing.module';
import { SvgIconComponent } from '../core/components/svg-icon/svg-icon.component';

@NgModule({
  declarations: [
    LoginComponent,
    UnauthorizedComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AuthRoutingModule,
    SvgIconComponent
  ]
})
export class AuthModule { }

