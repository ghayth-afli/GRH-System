import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotFoundComponent } from './pages/not-found/not-found.component';
import { UnauthorizedComponent } from './pages/unauthorized/unauthorized.component';



@NgModule({
  declarations: [
    NotFoundComponent,
    UnauthorizedComponent
  ],
  imports: [
    CommonModule
  ]
})
export class CoreModule { }
