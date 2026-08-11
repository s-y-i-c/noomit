"use client";

import { useEffect, useState } from "react";
import { CalendarClock } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { useGetMyAvailabilityQuery } from "../api/myAvailabilityApi";
import { AvailabilitySlotRow } from "./AvailabilitySlotRow";
import { DateNavigator } from "./DateNavigator";
import { SLOT_GENERATION_DAYS, addDays, toDateParam } from "./dateUtils";
import styles from "./MyAvailabilityManager.module.css";

function today(): string {
  return toDateParam(new Date());
}

const MIN_DATE = today();
const MAX_DATE = toDateParam(addDays(new Date(), SLOT_GENERATION_DAYS - 1));

export function MyAvailabilityManager() {
  const [selectedDate, setSelectedDate] = useState(MIN_DATE);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const { data: slots, isFetching, error } = useGetMyAvailabilityQuery(selectedDate);

  const errorMessage = error ? queryErrorMessage(error, "가능 시간 목록을 불러오지 못했습니다.") : null;

  useEffect(() => {
    if (!toastMessage) return;
    const timer = setTimeout(() => setToastMessage(null), 3000);
    return () => clearTimeout(timer);
  }, [toastMessage]);

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><CalendarClock size={15} /> Engineer</p>
          <h1>근무 일정 관리</h1>
          <p>날짜를 선택해 근무 가능 여부를 설정합니다.</p>
        </div>
      </header>

      <DateNavigator selectedDate={selectedDate} onSelect={setSelectedDate} minDate={MIN_DATE} maxDate={MAX_DATE} />

      {errorMessage ? <div className={styles.error}>{errorMessage}</div> : null}

      <div className={styles.list}>
        {isFetching ? (
          Array.from({ length: 4 }, (_, i) => <div key={i} className={styles.skeletonRow} />)
        ) : slots && slots.length > 0 ? (
          slots.map((slot) => (
            <AvailabilitySlotRow key={slot.slotId} slot={slot} date={selectedDate} onError={setToastMessage} />
          ))
        ) : (
          <div className={styles.empty}>해당 날짜에 등록된 시간대가 없습니다.</div>
        )}
      </div>

      {toastMessage ? <div className={styles.toast}>{toastMessage}</div> : null}
    </section>
  );
}