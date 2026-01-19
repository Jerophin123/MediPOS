export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: UserRole;
  active: boolean;
}

export enum UserRole {
  ADMIN = 'ADMIN',                    // All pages
  CASHIER = 'CASHIER',                // Billing
  STOCK_MONITOR = 'STOCK_MONITOR',    // Inventory
  STOCK_KEEPER = 'STOCK_KEEPER',      // Medicines
  CUSTOMER_SUPPORT = 'CUSTOMER_SUPPORT', // Returns
  ANALYST = 'ANALYST',                // Reports
  MANAGER = 'MANAGER'                 // Reports + Purchase History
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  username: string;
  fullName: string;
  email: string;
  role: string;
}



