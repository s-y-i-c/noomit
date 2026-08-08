"use client";

import { useState } from "react";
import { Wrench } from "lucide-react";
import { useGetMyRepairCasesQuery } from "../api/repairApi";
import type { RepairStatus } from "../types/repair";
import { RepairCaseDetail } from "./RepairCaseDetail";
import { statusLabel, statusBadgeData } from "./repairStatusUtils";
import styles from "./RepairCaseList.module.css";

const STATUS_TABS: Array<{ value: RepairStatus | "ALL"; label: string }> = [
  { value: "ALL", label: "전체" },
  { value: "IN_PROGRESS", label: "진행 중" },
  { value: "SUBMITTED", label: "제출됨" },
  { value: "COMPLETED", label: "완료" },
];

export function RepairCaseList() {
  const [tab, setTab] = useState<RepairStatus | "ALL">("ALL");
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const { data: cases, isFetching, error } = useGetMyRepairCasesQuery();

  const errorMessage =
    typeof error === "object" && error !== null && "message" in error ? String(error.message) : null;

  const filtered = cases
    ? tab === "ALL"
      ? cases
      : cases.filter((c) => c.status === tab)
    : [];

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><Wrench size={15} /> Repair Cases</p>
          <h1>담당 수리 건</h1>
          <p>배정된 수리 케이스를 확인하고 수리 내역을 기록합니다.</p>
        </div>
        {cases ? <span className={styles.total}>총 {cases.length}건</span> : null}
      </header>

      <div className={styles.tabs}>
        {STATUS_TABS.map((t) => (
          <button
            key={t.value}
            type="button"
            className={styles.tab}
            data-active={tab === t.value}
            onClick={() => setTab(t.value)}
          >
            {t.label}
            {cases && (
              <span className={styles.tabCount}>
                {t.value === "ALL" ? cases.length : cases.filter((c) => c.status === t.value).length}
              </span>
            )}
          </button>
        ))}
      </div>

      {errorMessage ? <div className={styles.error}>{errorMessage}</div> : null}

      <article className={styles.panel}>
        {filtered.length ? (
          <div className={styles.tableWrap}>
            <table>
              <thead>
                <tr>
                  <th>케이스 ID</th>
                  <th>상태</th>
                  <th>총 금액</th>
                  <th>수리 내역</th>
                  <th>반려 사유</th>
                  <th>수정일</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((c) => (
                  <tr key={c.id} className={styles.row} onClick={() => setSelectedId(c.id)}>
                    <td><strong>#{c.id}</strong></td>
                    <td>
                      <span className={styles.statusBadge} data-status={statusBadgeData(c.status)}>
                        {statusLabel(c.status)}
                      </span>
                    </td>
                    <td>{Number(c.totalAmount).toLocaleString()}원</td>
                    <td>{c.details.length}건</td>
                    <td>{c.rejectReason ?? "-"}</td>
                    <td><small>{new Date(c.updatedAt).toLocaleDateString("ko-KR")}</small></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className={styles.empty}>
            {isFetching ? "불러오는 중..." : "해당하는 수리 케이스가 없습니다."}
          </div>
        )}
      </article>

      {selectedId ? (
        <RepairCaseDetail caseId={selectedId} onClose={() => setSelectedId(null)} />
      ) : null}
    </section>
  );
}
