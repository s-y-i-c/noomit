export type RepairStatus = "IN_PROGRESS" | "SUBMITTED" | "COMPLETED";

export interface ServiceRequestSummary {
  id: string;
  requestNumber: string;
  symptom: string;
  modelName: string | null;
  subCategoryName: string | null;
  customerName: string;
  visitDate: string | null;
  visitStartTime: string | null;
  visitEndTime: string | null;
  address: string | null;
  detailAddress: string | null;
  remarks: string | null;
}

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
