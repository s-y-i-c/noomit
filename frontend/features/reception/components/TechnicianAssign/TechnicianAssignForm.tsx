"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, CalendarDays } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { useAssignmentSubmit } from "../../hooks/useAssignmentSubmit";
import { useGetServiceRequestDetailQuery } from "../../api/serviceRequestApi";
import {
  useGetAvailabilitySlotsQuery,
  useGetAvailableDatesQuery,
  useGetAvailableTechniciansQuery,
} from "../../api/technicianAvailabilityApi";
import type { AssignServiceRequestResponse, AvailabilityTimeSlot, AvailableTechnicianResponse } from "../../types/serviceRequest";
import { AssignmentCompletePanel } from "./AssignmentCompletePanel";
import { DateSelector } from "./DateSelector";
import { TimeSlotSelector } from "./TimeSlotSelector";
import { TechnicianSelector } from "./TechnicianSelector";
import styles from "./TechnicianAssignForm.module.css";

function toMinutePrefix(time: string): string {
  return time.slice(0, 5);
}

interface TechnicianAssignFormProps {
  serviceRequestId: string;
}

export function TechnicianAssignForm({ serviceRequestId }: TechnicianAssignFormProps) {
  const router = useRouter();
  const { requestNumber, actionLabel, cameFromDetail, isSubmitting, errorMessage: assignErrorMessage, submit } = useAssignmentSubmit(serviceRequestId);
  const { data: serviceRequest } = useGetServiceRequestDetailQuery(serviceRequestId);
  const baseFee = serviceRequest?.baseFee ?? null;

  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [selectedSlot, setSelectedSlot] = useState<AvailabilityTimeSlot | null>(null);
  const [selectedTechnician, setSelectedTechnician] = useState<AvailableTechnicianResponse | null>(null);
  const [assignResult, setAssignResult] = useState<AssignServiceRequestResponse | null>(null);

  const { data: availableDates, isFetching: datesLoading, error: datesError } = useGetAvailableDatesQuery();
  const { data: slots, isFetching: slotsLoading } = useGetAvailabilitySlotsQuery(selectedDate ?? "", { skip: !selectedDate });
  const { data: technicians, isFetching: techniciansLoading, error: techniciansError } = useGetAvailableTechniciansQuery(
    selectedSlot && selectedDate ? { date: selectedDate, startTime: selectedSlot.startTime, endTime: selectedSlot.endTime } : { date: "", startTime: "", endTime: "" },
    { skip: !selectedSlot || !selectedDate },
  );

  const datesErrorMessage = datesError ? queryErrorMessage(datesError, "배정 가능한 날짜를 불러오지 못했습니다.") : null;
  const techniciansErrorMessage = techniciansError ? queryErrorMessage(techniciansError, "가능한 기사 목록을 불러오지 못했습니다.") : null;

  const handleSelectDate = (date: string) => {
    setSelectedDate(date);
    setSelectedSlot(null);
    setSelectedTechnician(null);
  };

  const handleSelectSlot = (slot: AvailabilityTimeSlot) => {
    setSelectedSlot(slot);
    setSelectedTechnician(null);
  };

  const handleComplete = async () => {
    if (!selectedTechnician) return;
    try {
      const result = await submit(selectedTechnician.technicianId, selectedTechnician.slotId);
      setAssignResult(result);
    } catch {
      // 에러 메시지는 useAssignmentSubmit 훅에서 표시
    }
  };

  if (assignResult) {
    return (
      <AssignmentCompletePanel
        result={assignResult}
        requestNumber={requestNumber}
        baseFee={baseFee}
        title={`기사 ${actionLabel}이 완료됐습니다`}
        onConfirm={() => router.push("/counselor")}
      />
    );
  }

  return (
    <section className={styles.page}>
      {cameFromDetail ? (
        <button type="button" className={styles.backButton} onClick={() => router.back()}>
          <ArrowLeft size={16} /> 뒤로가기
        </button>
      ) : null}

      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><CalendarDays size={15} /> Reception</p>
          <h1>기사 {actionLabel}</h1>
          {requestNumber ? <p className={styles.requestNumberLarge}>접수번호 {requestNumber}</p> : null}
        </div>
      </header>

      <article className={styles.panel}>
        <h2 className={styles.sectionTitle}>날짜 선택</h2>
        <DateSelector
          dates={availableDates ?? []}
          isLoading={datesLoading}
          errorMessage={datesErrorMessage}
          selectedDate={selectedDate}
          onSelect={handleSelectDate}
        />
      </article>

      {selectedDate ? (
        <article className={styles.panel}>
          <h2 className={styles.sectionTitle}>시간 슬롯 선택</h2>
          <TimeSlotSelector
            slots={slots ?? []}
            isLoading={slotsLoading}
            selectedSlot={selectedSlot}
            onSelect={handleSelectSlot}
          />
        </article>
      ) : null}

      {selectedSlot ? (
        <article className={styles.panel}>
          <h2 className={styles.sectionTitle}>기사 선택</h2>
          <TechnicianSelector
            technicians={technicians ?? []}
            isLoading={techniciansLoading}
            errorMessage={techniciansErrorMessage}
            selectedTechnician={selectedTechnician}
            onSelect={setSelectedTechnician}
          />
        </article>
      ) : null}

      {assignErrorMessage ? <p className={styles.error}>{assignErrorMessage}</p> : null}

      <div className={styles.completeActions}>
        <div className={styles.selectionSummary}>
          {selectedTechnician && selectedSlot && selectedDate ? (
            <>
              <p><span>선택 기사</span>{selectedTechnician.technicianName}</p>
              <p><span>선택 일시</span>{selectedDate} {toMinutePrefix(selectedSlot.startTime)}~{toMinutePrefix(selectedSlot.endTime)}</p>
              {baseFee !== null ? <p><span>출장비</span>{baseFee.toLocaleString("ko-KR")}원</p> : null}
            </>
          ) : null}
        </div>
        <button
          type="button"
          className={styles.completeSubmit}
          disabled={!selectedTechnician || isSubmitting}
          onClick={handleComplete}
        >
          {isSubmitting ? `${actionLabel} 중...` : `${actionLabel} 완료`}
        </button>
      </div>
    </section>
  );
}