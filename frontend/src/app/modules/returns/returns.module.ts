import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ReturnsComponent } from './returns.component';
import { ReturnsRoutingModule } from './returns-routing.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';

@NgModule({
  declarations: [ReturnsComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ReturnsRoutingModule,
    SvgIconComponent
  ]
})
export class ReturnsModule { }

