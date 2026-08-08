"use client";

import { useState, type FormEvent } from "react";
import { ClipboardList, RefreshCw } from "lucide-react";
import {
  useGetAllRepairCasesQuery,
  useApproveRepairCaseMutation,
  useRejectRepairCaseMutation,
} from "../api/repairApi";
import type { RepairStatus } from "../types/repair";
import { statusLabel, statusBadgeData } from "./repairStatusUtils";
import { queryErrorMessage } from "@/features/store/api/queryError";
import styles from "./AdminRepairCaseList.module.css";

const STATUS_TABS: Array<{ value: RepairStatus | undefined; label: string }> = [
  { value: undefined, label: "전체" },
  { value: "IN_PROGRESS", label: "진행 중" },
  { value: "SUBMITTED", label: "제출됨" },
  { value: "COMPLETED", label: "완료" },
];

export function AdminRepairCaseList() {
  const [statusFilter, setStatusFilter] = useState<RepairStatus | undefined>(undefined);
  const [rejectTargetId, setRejectTargetId] = useState<string | null>(null);
  const [rejectReason, setRejectReason] = useState("");

  const { data: cases, isFetching, error } = useGetAllRepairCasesQuery(statusFilter);
  const [approve, { isLoading: isApproving }] = useApproveRepairCaseMutation();
  const [reject, { isLoading: isRejecting, error: rejectError }] = useRejectRepairCaseMutation();

  const errorMessage = queryErrorMessage(error, "");

  const handleApprove = (id: string) => {
    void approve(id);
  };

  const handleRejectSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!rejectTargetId || !rejectReason.trim()) return;
    const result = await reject({ id: rejectTargetId, reason: rejectReason.trim() });
    if (!("error" in result)) {
      setRejectTargetId(null);
      setRejectReason("");
    }
  };

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
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {cases.map((c) => (
                  <tr key={c.id}>
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
                    <td>
                      {c.status === "SUBMITTED" ? (
                        <div className={styles.actions}>
                          <button
                            type="button"
                            className={styles.approveButton}
                            onClick={() => handleApprove(c.id)}
                            disabled={isApproving}
                          >
                            {isApproving ? <RefreshCw size={12} className={styles.spinning} /> : null}
                            승인
                          </button>
                          <button
                            type="button"
                            className={styles.rejectButton}
                            onClick={() => { setRejectTargetId(c.id); setRejectReason(""); }}
                          >
                            반려
                          </button>
                        </div>
                      ) : (
                        <span className={styles.noAction}>-</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className={styles.empty}>해당하는 수리 케이스가 없습니다.</div>
        )}
      </article>

      {rejectTargetId ? (
        <div className={styles.backdrop} onClick={() => setRejectTargetId(null)}>
          <form className={styles.rejectModal} onClick={(e) => e.stopPropagation()} onSubmit={handleRejectSubmit}>
            <h3>반려 사유 입력</h3>
            <p>케이스 #{rejectTargetId}</p>
            <textarea
              className={styles.textarea}
              placeholder="반려 사유를 입력하세요"
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              rows={4}
              required
            />
            {rejectError ? (
              <p className={styles.formError}>{queryErrorMessage(rejectError, "반려에 실패했습니다.")}</p>
            ) : null}
            <div className={styles.rejectActions}>
              <button type="button" className={styles.cancelButton} onClick={() => setRejectTargetId(null)}>
                취소
              </button>
              <button type="submit" className={styles.confirmRejectButton} disabled={isRejecting}>
                {isRejecting ? <RefreshCw size={13} className={styles.spinning} /> : null}
                반려 확정
              </button>
            </div>
          </form>
        </div>
      ) : null}
    </section>
  );
}
