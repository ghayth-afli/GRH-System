import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from './sidebar/sidebar.component';
import { HeaderComponent } from './header/header.component';
import { RouterModule } from '@angular/router';
import { LayoutComponent } from './layout.component';
import { TimeAgoPipe } from '../shared/pipes/time-ago.pipe';
import { share } from 'rxjs';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [SidebarComponent, HeaderComponent, LayoutComponent],
  imports: [CommonModule, RouterModule, SharedModule],
  exports: [LayoutComponent],
})
export class LayoutModule {}
