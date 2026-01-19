import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserActivityRoutingModule } from './user-activity-routing.module';
import { UserActivityComponent } from './user-activity.component';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';

@NgModule({
  declarations: [
    UserActivityComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    UserActivityRoutingModule,
    SvgIconComponent
  ]
})
export class UserActivityModule { }



