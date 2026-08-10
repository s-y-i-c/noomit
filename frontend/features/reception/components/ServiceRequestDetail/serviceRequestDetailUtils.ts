export function toMinutePrefix(time: string): string {
  return time.slice(0, 5);
}

export function formatDateTime(iso: string | null): string | null {
  if (!iso) return null;
  const d = new Date(iso);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function formatFee(fee: number): string {
  return `${fee.toLocaleString("ko-KR")}원`;
}

export function formatVisit(date: string | null, start: string | null, end: string | null): string {
  if (!date || !start || !end) return "-";
  return `${date} ${toMinutePrefix(start)}~${toMinutePrefix(end)}`;
}