export type RequestStatus = "RECEIVED" | "ASSIGNED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

export interface StatisticsFilters {
  from: string;
  to: string;
  technicianId: string;
  customerId: string;
  productId: string;
  status: RequestStatus | "";
  repeatWindowDays: number;
}

export interface StatisticsDashboardData {
  period: { from: string; to: string };
  summary: {
    receivedCount: number;
    completedCount: number;
    inProgressCount: number;
    cancelledCount: number;
    completionRate: number;
    totalRepairAmount: number;
  };
  repeatRepair: {
    windowDays: number;
    sameCustomerRate: number;
    sameProductRate: number;
    sameTechnicianSameProductRate: number;
  };
  trends: Array<{ date: string; receivedCount: number }>;
  technicians: Array<{
    technicianId: string;
    technicianName: string;
    assignedCount: number;
    completedCount: number;
    completionRate: number;
    totalRepairAmount: number;
  }>;
  customers: Array<{
    customerId: string;
    customerName: string;
    requestCount: number;
    completedCount: number;
    repeatRate: number;
  }>;
  products: Array<{
    productId: string;
    productName: string;
    requestCount: number;
    completedCount: number;
    repeatRate: number;
  }>;
  integration: { connected: boolean; message: string };
}
