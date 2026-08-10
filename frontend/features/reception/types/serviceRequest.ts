export type ServiceRequestStatus = "RECEIVED" | "ASSIGNED" | "CANCELLED";

export interface ServiceRequestListItem {
  id: string;
  requestNumber: string;
  customerName: string;
  customerPhone: string;
  modelName: string;
  symptom: string;
  status: ServiceRequestStatus;
  receptionistName: string | null;
  technicianName: string | null;
  visitDate: string | null;
  visitStartTime: string | null;
  visitEndTime: string | null;
  requestedAt: string;
}

export interface ServiceRequestFilters {
  status: ServiceRequestStatus | "";
  sort: "requestedAt,desc" | "requestedAt,asc";
  page: number;
  size: number;
}

export interface ServiceRequestPageData {
  content: ServiceRequestListItem[];
  page: number;
  size: number;
  totalElements: number;
}