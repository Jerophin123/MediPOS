import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MedicinesComponent } from './medicines.component';
import { MedicinesRoutingModule } from './medicines-routing.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';

@NgModule({
  declarations: [MedicinesComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MedicinesRoutingModule,
    SvgIconComponent
  ]
})
export class MedicinesModule { }

