export type AvailabilityStatus = "AVAILABLE" | "UNAVAILABLE";

export interface MyAvailabilitySlot {
  slotId: string;
  startTime: string;
  endTime: string;
  status: AvailabilityStatus;
  isAssigned: boolean;
}