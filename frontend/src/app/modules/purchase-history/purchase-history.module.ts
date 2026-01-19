import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PurchaseHistoryComponent } from './purchase-history.component';
import { PurchaseHistoryRoutingModule } from './purchase-history-routing.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';

@NgModule({
  declarations: [PurchaseHistoryComponent],
  imports: [
    CommonModule,
    FormsModule,
    PurchaseHistoryRoutingModule,
    SvgIconComponent
  ]
})
export class PurchaseHistoryModule { }



