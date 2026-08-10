"use client";

import type { AvailableTechnicianResponse } from "../../types/serviceRequest";
import styles from "./TechnicianAssignForm.module.css";

interface TechnicianSelectorProps {
  technicians: AvailableTechnicianResponse[];
  isLoading: boolean;
  errorMessage: string | null;
  selectedTechnician: AvailableTechnicianResponse | null;
  onSelect: (technician: AvailableTechnicianResponse) => void;
}

export function TechnicianSelector({ technicians, isLoading, errorMessage, selectedTechnician, onSelect }: TechnicianSelectorProps) {
  if (errorMessage) return <p className={styles.error}>{errorMessage}</p>;
  if (isLoading) return <p className={styles.hint}>가능한 기사를 조회하는 중...</p>;
  if (technicians.length === 0) return <p className={styles.hint}>해당 시간에 가능한 기사가 없습니다.</p>;

  return (
    <div className={styles.technicianGrid}>
      {technicians.map((technician) => {
        const active = selectedTechnician?.slotId === technician.slotId;
        return (
          <button
            key={technician.slotId}
            type="button"
            className={styles.technicianCard}
            data-active={active}
            onClick={() => onSelect(technician)}
          >
            <span className={styles.technicianAvatar}>{technician.technicianName.charAt(0)}</span>
            <span className={styles.technicianName}>{technician.technicianName}</span>
            <span className={styles.technicianSelectLabel}>{active ? "선택됨" : "선택"}</span>
          </button>
        );
      })}
    </div>
  );
}