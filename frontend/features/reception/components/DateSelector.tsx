"use client";

import { useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import styles from "./TechnicianAssignForm.module.css";

const WEEKDAY_LABEL = ["일", "월", "화", "수", "목", "금", "토"];
const PAGE_SIZE = 7;

/** "YYYY-MM-DD" 문자열에서 요일만 뽑기 */
function parseDateParam(value: string): Date {
  const [y, m, d] = value.split("-").map(Number);
  return new Date(y, m - 1, d);
}

interface DateSelectorProps {
  dates: string[];
  isLoading: boolean;
  errorMessage: string | null;
  selectedDate: string | null;
  onSelect: (date: string) => void;
}

// 날짜 7개씩 페이지 나누기
export function DateSelector({ dates, isLoading, errorMessage, selectedDate, onSelect }: DateSelectorProps) {
  const [windowIndex, setWindowIndex] = useState(0);

  if (errorMessage) return <p className={styles.error}>{errorMessage}</p>;
  if (isLoading) return <p className={styles.hint}>날짜를 불러오는 중...</p>;
  if (dates.length === 0) return <p className={styles.hint}>배정 가능한 날짜가 없습니다.</p>;

  const visibleDates = dates.slice(windowIndex, windowIndex + PAGE_SIZE);
  const canGoPrev = windowIndex > 0;
  const canGoNext = windowIndex + PAGE_SIZE < dates.length;

  const goPrev = () => setWindowIndex((current) => Math.max(0, current - PAGE_SIZE));
  const goNext = () => setWindowIndex((current) => (current + PAGE_SIZE < dates.length ? current + PAGE_SIZE : current));

  return (
    <div className={styles.dateCarousel}>
      <button type="button" className={styles.navButton} onClick={goPrev} disabled={!canGoPrev} aria-label="이전 7일">
        <ChevronLeft size={16} />
      </button>
      <div className={styles.dateRow}>
        {visibleDates.map((date) => {
          const active = selectedDate === date;
          const parsed = parseDateParam(date);
          return (
            <button
              key={date}
              type="button"
              className={styles.dateButton}
              data-active={active}
              onClick={() => onSelect(date)}
            >
              <span>{parsed.getMonth() + 1}/{parsed.getDate()}</span>
              <small>({WEEKDAY_LABEL[parsed.getDay()]})</small>
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