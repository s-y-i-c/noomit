"use client";

import { useState } from "react";
import { ClipboardList } from "lucide-react";
import { useGetAllRepairCasesQuery } from "../api/repairApi";
import type { RepairStatus } from "../types/repair";
import { statusLabel, statusBadgeData } from "./repairStatusUtils";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { AdminRepairCaseDetail } from "./AdminRepairCaseDetail";
import styles from "./AdminRepairCaseList.module.css";

const STATUS_TABS: Array<{ value: RepairStatus | undefined; label: string }> = [
  { value: undefined, label: "전체" },
  { value: "IN_PROGRESS", label: "진행 중" },
  { value: "SUBMITTED", label: "제출됨" },
  { value: "COMPLETED", label: "완료" },
];

export function AdminRepairCaseList() {
  const [statusFilter, setStatusFilter] = useState<RepairStatus | undefined>(undefined);
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const { data: cases, isFetching, error } = useGetAllRepairCasesQuery(statusFilter, {
    pollingInterval: 10000,
  });

  const errorMessage = queryErrorMessage(error, "");

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><ClipboardList size={15} /> Repair Management</p>
          <h1>수리 건 관리</h1>
          <p>기사가 제출한 수리 케이스를 승인하거나 반려합니다.</p>
        </div>
        {cases ? <span className={styles.total}>총 {cases.length}건</span> : null}
      </header>

      <div className={styles.tabs}>
        {STATUS_TABS.map((t) => (
          <button
            key={String(t.value)}
            type="button"
            className={styles.tab}
            data-active={statusFilter === t.value}
            onClick={() => setStatusFilter(t.value)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {errorMessage ? <div className={styles.error}>{errorMessage}</div> : null}

      <article className={styles.panel}>
        {isFetching && !cases ? (
          <div className={styles.empty}>불러오는 중...</div>
        ) : cases && cases.length ? (
          <div className={styles.tableWrap}>
            <table>
              <thead>
                <tr>
                  <th>케이스 ID</th>
                  <th>기사 ID</th>
                  <th>상태</th>
                  <th>총 금액</th>
                  <th>수리 내역</th>
                  <th>수정일</th>
                </tr>
              </thead>
              <tbody>
                {cases.map((c) => (
                  <tr key={c.id} className={styles.row} onClick={() => setSelectedId(c.id)}>
                    <td><strong>#{c.id}</strong></td>
                    <td>{c.technicianId}</td>
                    <td>
                      <span className={styles.statusBadge} data-status={statusBadgeData(c.status)}>
                        {statusLabel(c.status)}
                      </span>
                    </td>
                    <td>{Number(c.totalAmount).toLocaleString()}원</td>
                    <td>{c.details.length}건</td>
                    <td><small>{new Date(c.updatedAt).toLocaleDateString("ko-KR")}</small></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className={styles.empty}>해당하는 수리 케이스가 없습니다.</div>
        )}
      </article>

      {selectedId ? (
        <AdminRepairCaseDetail caseId={selectedId} onClose={() => setSelectedId(null)} />
      ) : null}
    </section>
  );
}
