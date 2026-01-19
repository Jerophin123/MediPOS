export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login'
  },
  BILLING: {
    CREATE: '/cashier/bills',
    GET_BY_ID: (id: number) => `/cashier/bills/${id}`,
    GET_BY_NUMBER: (billNumber: string) => `/cashier/bills/number/${billNumber}`,
    CANCEL: (id: number) => `/cashier/bills/${id}/cancel`
  },
  MEDICINES: {
    LIST: '/pharmacist/medicines',
    GET_BY_ID: (id: number) => `/pharmacist/medicines/${id}`,
    SEARCH: (name: string) => `/pharmacist/medicines/search?name=${encodeURIComponent(name)}`,
    CREATE: '/pharmacist/medicines',
    UPDATE_STATUS: (id: number, status: string) => `/pharmacist/medicines/${id}/status?status=${status}`
  },
  BATCHES: {
    BY_MEDICINE: (medicineId: number) => `/pharmacist/batches/medicine/${medicineId}`,
    CREATE: '/pharmacist/batches',
    EXPIRED: '/pharmacist/batches/expired',
    LOW_STOCK: (threshold: number) => `/pharmacist/batches/low-stock?threshold=${threshold}`
  },
  RETURNS: {
    PROCESS: '/cashier/returns'
  },
  REPORTS: {
    SALES: (startDate: string, endDate: string) => `/admin/reports/sales?startDate=${startDate}&endDate=${endDate}`,
    GST: (startDate: string, endDate: string) => `/admin/reports/gst?startDate=${startDate}&endDate=${endDate}`,
    CASHIER: (cashierId: number, startDate: string, endDate: string) => 
      `/admin/reports/cashier/${cashierId}?startDate=${startDate}&endDate=${endDate}`
  }
};





