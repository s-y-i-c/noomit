export type RepairStatus = "IN_PROGRESS" | "SUBMITTED" | "COMPLETED";

export interface RepairDetail {
  id: string;
  repairCaseId: string;
  description: string;
  amount: string;
  createdAt: string;
}

export interface RepairCase {
  id: string;
  serviceRequestId: string;
  technicianId: string;
  status: RepairStatus;
  totalAmount: string;
  rejectReason: string | null;
  createdAt: string;
  updatedAt: string;
  details: RepairDetail[];
}
