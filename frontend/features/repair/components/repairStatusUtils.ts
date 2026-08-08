import type { RepairStatus } from "../types/repair";

export function statusLabel(status: RepairStatus): string {
  switch (status) {
    case "IN_PROGRESS": return "진행 중";
    case "SUBMITTED": return "제출됨";
    case "COMPLETED": return "완료";
  }
}

export function statusBadgeData(status: RepairStatus): string {
  return status.toLowerCase().replace("_", "-");
}
