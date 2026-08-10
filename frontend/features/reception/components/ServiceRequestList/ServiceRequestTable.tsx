"use client";

import { useRouter } from "next/navigation";
import type { ServiceRequestListItem } from "../../types/serviceRequest";
import { statusLabel, statusBadgeData } from "../serviceRequestStatusUtils";
import styles from "./ServiceRequestList.module.css";

function toMinute(time: string): string {
  return time.slice(0, 5);
}

function formatVisit(visitDate: string | null, visitStartTime: string | null, visitEndTime: string | null): string {
  if (!visitDate || !visitStartTime || !visitEndTime) return "-";
  return `${visitDate} ${toMinute(visitStartTime)}~${toMinute(visitEndTime)}`;
}

interface ServiceRequestTableProps {
  items: ServiceRequestListItem[];
}

export function ServiceRequestTable({ items }: ServiceRequestTableProps) {
  const router = useRouter();

  return (
    <div className={styles.tableWrap}>
      <table>
        <thead>
          <tr>
            <th>접수번호</th>
            <th>고객명</th>
            <th>전화번호</th>
            <th>제품 모델명</th>
            <th>증상</th>
            <th>상태</th>
            <th>접수자</th>
            <th>담당기사</th>
            <th>방문예정일시</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr
              key={item.id}
              className={styles.row}
              onClick={() => router.push(`/counselor/reception/${item.id}`)}
            >
              <td>{item.requestNumber}</td>
              <td><strong>{item.customerName}</strong></td>
              <td>{item.customerPhone}</td>
              <td>{item.modelName ?? "-"}</td>
              <td>
                <span className={styles.symptomText} title={item.symptom}>
                  {item.symptom}
                </span>
              </td>
              <td>
                <span className={styles.statusBadge} data-status={statusBadgeData(item.status)}>
                  {statusLabel(item.status)}
                </span>
              </td>
              <td>{item.receptionistName ?? "-"}</td>
              <td>{item.technicianName ?? "-"}</td>
              <td>{formatVisit(item.visitDate, item.visitStartTime, item.visitEndTime)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}