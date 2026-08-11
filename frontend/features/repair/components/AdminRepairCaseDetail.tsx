"use client";

import { useState, type FormEvent } from "react";
import { X, RefreshCw, Check, XCircle } from "lucide-react";
import {
  useGetRepairCaseQuery,
  useGetServiceRequestSummaryQuery,
  useApproveRepairCaseMutation,
  useRejectRepairCaseMutation,
} from "../api/repairApi";
import { statusLabel, statusBadgeData } from "./repairStatusUtils";
import { queryErrorMessage } from "@/features/store/api/queryError";
import styles from "./AdminRepairCaseDetail.module.css";

interface Props {
  caseId: string;
  onClose: () => void;
}

export function AdminRepairCaseDetail({ caseId, onClose }: Props) {
  const { data: repairCase, isFetching, error } = useGetRepairCaseQuery(caseId);
  const { data: srSummary } = useGetServiceRequestSummaryQuery(
    repairCase?.serviceRequestId ?? "",
    { skip: !repairCase?.serviceRequestId }
  );
  const [approve, { isLoading: isApproving, error: approveError }] = useApproveRepairCaseMutation();
  const [reject, { isLoading: isRejecting, error: rejectError }] = useRejectRepairCaseMutation();

  const [showRejectForm, setShowRejectForm] = useState(false);
  const [rejectReason, setRejectReason] = useState("");

  const isSubmitted = repairCase?.status === "SUBMITTED";
  const fetchError = queryErrorMessage(error, "수리 케이스를 불러오지 못했습니다.");

  const handleApprove = async () => {
    const result = await approve(caseId);
    if (!("error" in result)) onClose();
  };

  const handleRejectSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!rejectReason.trim()) return;
    const result = await reject({ id: caseId, reason: rejectReason.trim() });
    if (!("error" in result)) onClose();
  };

  return (
    <div className={styles.backdrop} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <div>
            <h2>수리 케이스 #{caseId}</h2>
            {repairCase ? (
              <span className={styles.statusBadge} data-status={statusBadgeData(repairCase.status)}>
                {statusLabel(repairCase.status)}
              </span>
            ) : null}
          </div>
          <button type="button" onClick={onClose} aria-label="닫기" className={styles.closeButton}>
            <X size={18} />
          </button>
        </div>

        {isFetching ? <div className={styles.state}>불러오는 중...</div> : null}
        {error ? <div className={styles.stateError}>{fetchError}</div> : null}

        {repairCase ? (
          <>
            {srSummary ? (
              <section className={styles.srSection}>
                <h3>접수 정보</h3>
                <dl className={styles.srGrid}>
                  <dt>접수번호</dt><dd>{srSummary.requestNumber}</dd>
                  <dt>고객</dt><dd>{srSummary.customerName}</dd>
                  <dt>제품</dt><dd>{[srSummary.subCategoryName, srSummary.modelName].filter(Boolean).join(" / ") || "-"}</dd>
                  <dt>증상</dt><dd>{srSummary.symptom}</dd>
                  {srSummary.visitDate ? (
                    <>
                      <dt>방문일</dt>
                      <dd>{srSummary.visitDate} {srSummary.visitStartTime ?? ""} ~ {srSummary.visitEndTime ?? ""}</dd>
                    </>
                  ) : null}
                  {srSummary.address ? (
                    <>
                      <dt>주소</dt>
                      <dd>{srSummary.address} {srSummary.detailAddress ?? ""}</dd>
                    </>
                  ) : null}
                </dl>
              </section>
            ) : null}

            {repairCase.rejectReason ? (
              <div className={styles.rejectReason}>
                <strong>반려 사유:</strong> {repairCase.rejectReason}
              </div>
            ) : null}

            <div className={styles.summary}>
              <span>총 금액</span>
              <strong>{Number(repairCase.totalAmount).toLocaleString()}원</strong>
            </div>

            <section className={styles.detailsSection}>
              <h3>수리 내역 ({repairCase.details.length}건)</h3>
              {repairCase.details.length ? (
                <ul className={styles.detailList}>
                  {repairCase.details.map((d) => (
                    <li key={d.id} className={styles.detailItem}>
                      <span className={styles.detailDescription}>{d.description}</span>
                      <span className={styles.detailAmount}>{Number(d.amount).toLocaleString()}원</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className={styles.emptyDetails}>등록된 수리 내역이 없습니다.</p>
              )}
            </section>

            {isSubmitted ? (
              showRejectForm ? (
                <form className={styles.rejectForm} onSubmit={handleRejectSubmit}>
                  <h3>반려 사유</h3>
                  <textarea
                    className={styles.textarea}
                    placeholder="반려 사유를 입력하세요"
                    value={rejectReason}
                    onChange={(e) => setRejectReason(e.target.value)}
                    rows={3}
                    required
                    autoFocus
                  />
                  {rejectError ? (
                    <p className={styles.formError}>{queryErrorMessage(rejectError, "반려에 실패했습니다.")}</p>
                  ) : null}
                  <div className={styles.rejectFormActions}>
                    <button type="button" className={styles.cancelButton} onClick={() => setShowRejectForm(false)}>
                      취소
                    </button>
                    <button type="submit" className={styles.confirmRejectButton} disabled={isRejecting}>
                      {isRejecting ? <RefreshCw size={13} className={styles.spinning} /> : <XCircle size={14} />}
                      반려 확정
                    </button>
                  </div>
                </form>
              ) : (
                <div className={styles.actionRow}>
                  {approveError ? (
                    <p className={styles.formError}>{queryErrorMessage(approveError, "승인에 실패했습니다.")}</p>
                  ) : null}
                  <button
                    type="button"
                    className={styles.rejectButton}
                    onClick={() => setShowRejectForm(true)}
                  >
                    <XCircle size={15} />
                    반려
                  </button>
                  <button
                    type="button"
                    className={styles.approveButton}
                    onClick={() => void handleApprove()}
                    disabled={isApproving}
                  >
                    {isApproving ? <RefreshCw size={15} className={styles.spinning} /> : <Check size={15} />}
                    승인
                  </button>
                </div>
              )
            ) : null}
          </>
        ) : null}
      </div>
    </div>
  );
}
