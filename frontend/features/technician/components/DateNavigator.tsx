"use client";

import { useMemo, useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { addDays, parseDateParam, toDateParam, weekdayShort } from "./dateUtils";
import styles from "./DateNavigator.module.css";

const PAGE_SIZE = 7;

interface DateNavigatorProps {
  selectedDate: string;
  onSelect: (date: string) => void;
  /** 둘 다 지정하면 이 범위 밖의 날짜는 창에서 걸러짐 */
  minDate?: string;
  maxDate?: string;
}

// 화살표는 7일 단위로 보이는 창만 이동. minDate/maxDate가 있으면 범위 밖 날짜는 걸러서 보여줌
export function DateNavigator({ selectedDate, onSelect, minDate, maxDate }: DateNavigatorProps) {
  const minDateObj = minDate ? parseDateParam(minDate) : null;
  const maxDateObj = maxDate ? parseDateParam(maxDate) : null;

  // windowStart: 현재 화면에 표시할 7일의 시작 날짜
  const [windowStart, setWindowStart] = useState(() => parseDateParam(selectedDate));

  const dates = useMemo(() => {
    const raw = Array.from({ length: PAGE_SIZE }, (_, i) => addDays(windowStart, i));
    if (!minDateObj && !maxDateObj) return raw;
    return raw.filter((d) => {
        if (minDateObj && d < minDateObj) { return false; }
        if (maxDateObj && d > maxDateObj) { return false; }
        return true;
        });
  }, [windowStart, minDateObj, maxDateObj]);

  const windowEnd = addDays(windowStart, PAGE_SIZE - 1);
  const canGoPrev = !minDateObj || windowStart > minDateObj;
  const canGoNext = !maxDateObj || windowEnd < maxDateObj;

  const goPrev = () => setWindowStart((current) => addDays(current, -PAGE_SIZE));
  const goNext = () => setWindowStart((current) => addDays(current, PAGE_SIZE));

  return (
    <div className={styles.navigator}>
      <button type="button" className={styles.navButton} onClick={goPrev} disabled={!canGoPrev} aria-label="이전 7일">
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
      <button type="button" className={styles.navButton} onClick={goNext} disabled={!canGoNext} aria-label="다음 7일">
        <ChevronRight size={16} />
      </button>
    </div>
  );
}