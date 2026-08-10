"use client";

import { useMemo, useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { addDays, parseDateParam, toDateParam, weekdayShort } from "./dateUtils";
import styles from "./DateNavigator.module.css";

interface DateNavigatorProps {
  selectedDate: string;
  onSelect: (date: string) => void;
}

// 화살표는 7일 단위로 보이는 창만 이동
export function DateNavigator({ selectedDate, onSelect }: DateNavigatorProps) {
  // windowStart: 현재 화면에 표시할 7일의 시작 날짜
  const [windowStart, setWindowStart] = useState(() => parseDateParam(selectedDate));

  const dates = useMemo(
    () => Array.from({ length: 7 }, (_, i) => addDays(windowStart, i)),
    [windowStart],
  );

  const goPrev = () => setWindowStart((current) => addDays(current, -7));
  const goNext = () => setWindowStart((current) => addDays(current, 7));

  return (
    <div className={styles.navigator}>
      <button type="button" className={styles.navButton} onClick={goPrev} aria-label="이전 7일">
        <ChevronLeft size={16} />
      </button>
      <div className={styles.dateRow}>
        {dates.map((date) => {
          const value = toDateParam(date);
          const active = selectedDate === value;
          return (
            <button
              key={value}
              type="button"
              className={styles.dateChip}
              data-active={active}
              onClick={() => onSelect(value)}
            >
              <span>{date.getMonth() + 1}/{date.getDate()}</span>
              <small>({weekdayShort(date)})</small>
            </button>
          );
        })}
      </div>
      <button type="button" className={styles.navButton} onClick={goNext} aria-label="다음 7일">
        <ChevronRight size={16} />
      </button>
    </div>
  );
}