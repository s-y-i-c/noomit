"use client";

import { useUpdateMyAvailabilityMutation } from "../api/myAvailabilityApi";
import type { MyAvailabilitySlot } from "../types/myAvailability";
import { AvailabilityToggle } from "./AvailabilityToggle";
import { toMinutePrefix } from "./dateUtils";
import styles from "./AvailabilitySlotRow.module.css";

interface AvailabilitySlotRowProps {
  slot: MyAvailabilitySlot;
  date: string;
  onError: (message: string) => void;
}

export function AvailabilitySlotRow({ slot, date, onError }: AvailabilitySlotRowProps) {
  const [updateAvailability] = useUpdateMyAvailabilityMutation();

  const statusText = slot.isAssigned
    ? "예약됨"
    : slot.status === "AVAILABLE"
      ? "근무 가능"
      : "근무 불가";

  const checked = !slot.isAssigned && slot.status === "AVAILABLE";

  const handleToggle = async () => {
    const nextStatus = slot.status === "AVAILABLE" ? "UNAVAILABLE" : "AVAILABLE";
    try {
      await updateAvailability({ slotId: slot.slotId, status: nextStatus, date }).unwrap();
    } catch {
      onError("가능 시간을 변경하지 못했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <div className={styles.row}>
      <span className={styles.time}>
        {toMinutePrefix(slot.startTime)}~{toMinutePrefix(slot.endTime)}
      </span>
      <span className={styles.statusText} data-dimmed={slot.isAssigned}>{statusText}</span>
      <AvailabilityToggle
        checked={checked}
        disabled={slot.isAssigned}
        ariaLabel={statusText}
        onToggle={slot.isAssigned ? undefined : handleToggle}
      />
    </div>
  );
}