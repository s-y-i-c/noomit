import type { ServiceRequestStatus } from "../types/serviceRequest";

export function statusLabel(status: ServiceRequestStatus): string {
  switch (status) {
    case "RECEIVED": return "접수";
    case "ASSIGNED": return "배정";
    case "CANCELLED": return "취소";
  }
}

export function statusBadgeData(status: ServiceRequestStatus): string {
  return status.toLowerCase();
}