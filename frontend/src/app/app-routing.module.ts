import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth/auth.guard';
import { RoleGuard } from './auth/role.guard';
import { UserRole } from './core/models/user.model';

const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'login',
    redirectTo: '/auth/login',
    pathMatch: 'full'
  },
  {
    path: 'billing',
    loadChildren: () => import('./modules/billing/billing.module').then(m => m.BillingModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: [UserRole.CASHIER, UserRole.ADMIN] }
  },
  {
    path: 'inventory',
    loadChildren: () => import('./modules/inventory/inventory.module').then(m => m.InventoryModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: [UserRole.STOCK_MONITOR, UserRole.ADMIN] }
  },
  {
    path: 'medicines',
    loadChildren: () => import('./modules/medicines/medicines.module').then(m => m.MedicinesModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: [UserRole.STOCK_KEEPER, UserRole.ADMIN] }
  },
  {
    path: 'returns',
    loadChildren: () => import('./modules/returns/returns.module').then(m => m.ReturnsModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: [UserRole.CUSTOMER_SUPPORT, UserRole.ADMIN] }
  },
  {
    path: 'reports',
    loadChildren: () => import('./modules/reports/reports.module').then(m => m.ReportsModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: [UserRole.ANALYST, UserRole.MANAGER, UserRole.ADMIN] }
  },
  {
    path: 'purchase-history',
    loadChildren: () => import('./modules/purchase-history/purchase-history.module').then(m => m.PurchaseHistoryModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: [UserRole.MANAGER, UserRole.ADMIN] }
  },
  {
    path: 'user-activity',
    loadChildren: () => import('./modules/user-activity/user-activity.module').then(m => m.UserActivityModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: [UserRole.ADMIN] }
  },
  {
    path: 'login-history',
    loadChildren: () => import('./modules/login-history/login-history.module').then(m => m.LoginHistoryModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: [UserRole.ADMIN] }
  },
  {
    path: 'user-management',
    loadChildren: () => import('./modules/user-management/user-management.module').then(m => m.UserManagementModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: [UserRole.ADMIN] }
  },
  {
    path: '',
    redirectTo: '/billing',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/billing'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }



