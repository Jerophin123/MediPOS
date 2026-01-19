import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { InventoryComponent } from './inventory.component';
import { InventoryRoutingModule } from './inventory-routing.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';

@NgModule({
  declarations: [InventoryComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InventoryRoutingModule,
    SvgIconComponent
  ]
})
export class InventoryModule { }

