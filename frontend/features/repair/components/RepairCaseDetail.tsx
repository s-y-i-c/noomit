"use client";

import { useState, type FormEvent } from "react";
import { Trash2, X, Plus, RefreshCw } from "lucide-react";
import {
  useGetRepairCaseQuery,
  useGetServiceRequestSummaryQuery,
  useAddRepairDetailMutation,
  useDeleteRepairDetailMutation,
  useSubmitRepairCaseMutation,
} from "../api/repairApi";
import { statusLabel, statusBadgeData } from "./repairStatusUtils";
import { queryErrorMessage } from "@/features/store/api/queryError";
import styles from "./RepairCaseDetail.module.css";

interface RepairCaseDetailProps {
  caseId: string;
  onClose: () => void;
}

export function RepairCaseDetail({ caseId, onClose }: RepairCaseDetailProps) {
  const { data: repairCase, isFetching, error } = useGetRepairCaseQuery(caseId);
  const { data: srSummary } = useGetServiceRequestSummaryQuery(
    repairCase?.serviceRequestId ?? "",
    { skip: !repairCase?.serviceRequestId }
  );
  const [addDetail, { isLoading: isAdding, error: addError }] = useAddRepairDetailMutation();
  const [deleteDetail, { isLoading: isDeleting }] = useDeleteRepairDetailMutation();
  const [submit, { isLoading: isSubmitting, error: submitError }] = useSubmitRepairCaseMutation();

  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");

  const isInProgress = repairCase?.status === "IN_PROGRESS";

  const handleAddDetail = async (e: FormEvent) => {
    e.preventDefault();
    if (!description.trim() || !amount.trim()) return;
    const result = await addDetail({ id: caseId, description: description.trim(), amount: parseFloat(amount) });
    if (!("error" in result)) {
      setDescription("");
      setAmount("");
    }
  };

  const handleDeleteDetail = (detailId: string) => {
    void deleteDetail({ id: caseId, detailId });
  };

  const handleSubmit = () => {
    void submit(caseId);
  };

  const fetchError = queryErrorMessage(error, "수리 케이스를 불러오지 못했습니다.");

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
                  {srSummary.remarks ? (
                    <>
                      <dt>비고</dt><dd>{srSummary.remarks}</dd>
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
                      {isInProgress ? (
                        <button
                          type="button"
                          className={styles.deleteButton}
                          onClick={() => handleDeleteDetail(d.id)}
                          disabled={isDeleting}
                          aria-label="삭제"
                        >
                          <Trash2 size={14} />
                        </button>
                      ) : null}
                    </li>
                  ))}
                </ul>
              ) : (
                <p className={styles.emptyDetails}>등록된 수리 내역이 없습니다.</p>
              )}
            </section>

            {isInProgress ? (
              <>
                <form className={styles.addForm} onSubmit={handleAddDetail}>
                  <h3>수리 내역 추가</h3>
                  <div className={styles.formRow}>
                    <input
                      className={styles.input}
                      placeholder="수리 내용"
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      required
                    />
                    <input
                      className={styles.input}
                      placeholder="금액"
                      type="number"
                      min="0"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      required
                    />
                    <button type="submit" className={styles.addButton} disabled={isAdding}>
                      {isAdding ? <RefreshCw size={14} className={styles.spinning} /> : <Plus size={14} />}
                      추가
                    </button>
                  </div>
                  {addError ? (
                    <p className={styles.formError}>{queryErrorMessage(addError, "추가에 실패했습니다.")}</p>
                  ) : null}
                </form>

                <button
                  type="button"
                  className={styles.submitButton}
                  onClick={handleSubmit}
                  disabled={isSubmitting || repairCase.details.length === 0}
                >
                  {isSubmitting ? <RefreshCw size={15} className={styles.spinning} /> : null}
                  완료 제출
                </button>
                {submitError ? (
                  <p className={styles.formError}>{queryErrorMessage(submitError, "제출에 실패했습니다.")}</p>
                ) : null}
              </>
            ) : null}
          </>
        ) : null}
      </div>
    </div>
  );
}
