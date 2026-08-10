"use client";

import type { AvailabilityTimeSlot } from "../../types/serviceRequest";
import styles from "./TechnicianAssignForm.module.css";

function toMinutePrefix(time: string): string {
  return time.slice(0, 5);
}

interface TimeSlotSelectorProps {
  slots: AvailabilityTimeSlot[];
  isLoading: boolean;
  selectedSlot: { startTime: string; endTime: string } | null;
  onSelect: (slot: AvailabilityTimeSlot) => void;
}

export function TimeSlotSelector({ slots, isLoading, selectedSlot, onSelect }: TimeSlotSelectorProps) {
  if (isLoading) return <p className={styles.hint}>시간대를 불러오는 중...</p>;
  if (slots.length === 0) return <p className={styles.hint}>등록된 시간대가 없습니다.</p>;

  return (
    <div className={styles.slotRow}>
      {slots.map((slot) => {
        const active = selectedSlot?.startTime === slot.startTime;
        return (
          <button
            key={slot.startTime}
            type="button"
            className={styles.slotButton}
            data-active={active}
            disabled={!slot.available}
            onClick={() => slot.available && onSelect(slot)}
          >
            {toMinutePrefix(slot.startTime)}~{toMinutePrefix(slot.endTime)}
          </button>
        );
      })}
    </div>
  );
}