/** 기사 슬롯이 미리 생성돼 있는 기간(오늘 포함 N일) */
export const SLOT_GENERATION_DAYS = 30;

const WEEKDAY_SHORT = ["일", "월", "화", "수", "목", "금", "토"];
const WEEKDAY_FULL = ["일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"];

/** Date 객체 -> YYYY-MM-DD 문자열 **/
export function toDateParam(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** "YYYY-MM-DD" 문자열 -> Date 객체 */
export function parseDateParam(value: string): Date {
  const [y, m, d] = value.split("-").map(Number);
  return new Date(y, m - 1, d);
}

export function addDays(date: Date, amount: number): Date {
  const next = new Date(date);
  next.setDate(next.getDate() + amount);
  return next;
}

export function weekdayShort(date: Date): string {
  return WEEKDAY_SHORT[date.getDay()];
}

export function weekdayFull(date: Date): string {
  return WEEKDAY_FULL[date.getDay()];
}

export function toMinutePrefix(time: string): string {
  return time.slice(0, 5);
}