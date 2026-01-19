import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ReportsComponent } from './reports.component';
import { ReportsRoutingModule } from './reports-routing.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';

@NgModule({
  declarations: [ReportsComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ReportsRoutingModule,
    SvgIconComponent
  ]
})
export class ReportsModule { }

