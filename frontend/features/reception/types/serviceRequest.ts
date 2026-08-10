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

export interface CreateServiceRequestRequest {
  customerId: string;
  productId: string;
  symptom: string;
  remarks: string;
}

export interface ServiceRequestCreateResponse {
  id: string;
  requestNumber: string;
  status: ServiceRequestStatus;
  symptom: string;
  remarks: string;
  baseFee: number;
  requestedAt: string;
}

export interface AvailabilityTimeSlot {
  startTime: string;
  endTime: string;
  available: boolean;
}

export interface AvailableTechnicianResponse {
  technicianId: string;
  technicianName: string;
  slotId: string;
  startTime: string;
  endTime: string;
}

export interface AssignServiceRequestRequest {
  technicianId: string;
  slotId: string;
}

export interface AssignServiceRequestResponse {
  id: string;
  status: ServiceRequestStatus;
  technicianName: string;
  visitDate: string;
  visitStartTime: string;
  visitEndTime: string;
}