import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AuthRedirectGuard } from './core/guards/auth-redirect.guard';
import { LayoutComponent } from './layout/layout.component';
import { RoleGuard } from './core/guards/role.guard';
import { LeaveBalanceResolver } from './modules/leave/resolvers/leave-balance.resolver';

const routes: Routes = [
  { path: '', redirectTo: '', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () =>
      import('./modules/auth/auth.module').then((m) => m.AuthModule),
    canActivate: [AuthRedirectGuard],
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    resolve: {
      leaveBalance: LeaveBalanceResolver,
    },
    children: [
      {
        path: 'home',
        loadChildren: () =>
          import('./modules/home/home.module').then((m) => m.HomeModule),
        canActivate: [RoleGuard],
        data: { roles: ['Employee'] },
      },
      {
        path: 'leave',

        loadChildren: () =>
          import('./modules/leave/leave.module').then((m) => m.LeaveModule),
        canActivate: [RoleGuard],
        data: { roles: ['Manager', 'HR'] },
      },
      {
        path: 'medical-visits',
        loadChildren: () =>
          import('./modules/med-visit/med-visit.module').then(
            (m) => m.MedVisitModule
          ),
        canActivate: [RoleGuard],
        data: { roles: ['Manager', 'HR', 'Employee'] },
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: 'home' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
