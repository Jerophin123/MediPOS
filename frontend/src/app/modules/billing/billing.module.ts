import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { BillingComponent } from './billing.component';
import { BillingRoutingModule } from './billing-routing.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';

@NgModule({
  declarations: [
    BillingComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    BillingRoutingModule,
    SvgIconComponent
  ]
})
export class BillingModule { }

