"use client";

import { useState } from "react";
import { RefreshCw, X } from "lucide-react";
import { useChangeCustomerStatusMutation, useGetCustomerByIdQuery } from "../api/customersApi";
import type { CustomerStatus } from "../types/customer";
import styles from "./CustomerDetailModal.module.css";

interface CustomerDetailModalProps {
  customerId: string;
  onClose: () => void;
}

export function CustomerDetailModal({ customerId, onClose }: CustomerDetailModalProps) {
  const { data: customer, isFetching, error } = useGetCustomerByIdQuery(customerId);
  const [changeStatus, { isLoading: isChangingStatus, error: changeStatusError }] =
    useChangeCustomerStatusMutation();
  const [pendingStatus, setPendingStatus] = useState<CustomerStatus | null>(null);

  const errorMessage = typeof error === "object" && error !== null && "message" in error
    ? String(error.message)
    : null;
  const changeStatusErrorMessage =
    typeof changeStatusError === "object" && changeStatusError !== null && "message" in changeStatusError
      ? String(changeStatusError.message)
      : null;

  const requestStatusChange = () => {
    if (!customer) return;
    setPendingStatus(customer.status === "ACTIVE" ? "INACTIVE" : "ACTIVE");
  };

  const confirmStatusChange = async () => {
    if (!customer || !pendingStatus) return;
    const result = await changeStatus({ id: customer.id, status: pendingStatus });
    // 실패했으면 에러 메시지를 보여줘야 하니 확인창을 그대로 열어둔다.
    if (!("error" in result)) setPendingStatus(null);
  };

  return (
    <div className={styles.backdrop} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2>고객 상세 정보</h2>
          <button type="button" onClick={onClose} aria-label="닫기" className={styles.closeButton}>
            <X size={18} />
          </button>
        </div>

        {isFetching ? <div className={styles.state}>불러오는 중...</div> : null}
        {errorMessage ? <div className={styles.state}>{errorMessage}</div> : null}

        {customer ? (
          <>
            <dl className={styles.fields}>
              <div className={styles.field}>
                <dt>이름</dt>
                <dd>{customer.name}</dd>
              </div>
              <div className={styles.field}>
                <dt>전화번호</dt>
                <dd>{customer.phoneNumber}</dd>
              </div>
              <div className={styles.field}>
                <dt>상태</dt>
                <dd>
                  <button
                    type="button"
                    className={styles.statusButton}
                    data-status={customer.status}
                    onClick={requestStatusChange}
                    disabled={isChangingStatus}
                  >
                    {isChangingStatus ? (
                      <RefreshCw className={styles.spinning} size={12} />
                    ) : null}
                    {customer.status === "ACTIVE" ? "활성" : "비활성"}
                  </button>
                </dd>
              </div>
              <div className={styles.field}>
                <dt>우편번호</dt>
                <dd>{customer.zipCode}</dd>
              </div>
              <div className={styles.field}>
                <dt>주소</dt>
                <dd>{customer.address}</dd>
              </div>
              <div className={styles.field}>
                <dt>상세주소</dt>
                <dd>{customer.detailAddress ?? "-"}</dd>
              </div>
              <div className={styles.field}>
                <dt>메모</dt>
                <dd>{customer.memo ?? "-"}</dd>
              </div>
              <div className={styles.field}>
                <dt>고객 ID</dt>
                <dd>{customer.id}</dd>
              </div>
            </dl>
          </>
        ) : null}
      </div>

      {pendingStatus ? (
        <div
          className={styles.confirmBackdrop}
          onClick={(e) => {
            e.stopPropagation();
            setPendingStatus(null);
          }}
        >
          <div className={styles.confirmModal} onClick={(e) => e.stopPropagation()}>
            <h3>
              {customer?.name}님을 {pendingStatus === "ACTIVE" ? "활성" : "비활성"} 상태로
              전환할까요?
            </h3>
            {pendingStatus === "INACTIVE" ? (
              <p className={styles.confirmHint}>
                비활성화해도, 같은 전화번호로 새 접수가 들어오면 자동으로 다시 활성화됩니다.
              </p>
            ) : null}
            {changeStatusErrorMessage ? <p className={styles.confirmError}>{changeStatusErrorMessage}</p> : null}
            <div className={styles.confirmActions}>
              <button
                type="button"
                className={styles.outlineButton}
                onClick={() => setPendingStatus(null)}
                disabled={isChangingStatus}
              >
                취소
              </button>
              <button
                type="button"
                className={pendingStatus === "INACTIVE" ? styles.dangerButton : styles.primaryButton}
                onClick={confirmStatusChange}
                disabled={isChangingStatus}
              >
                {isChangingStatus ? <RefreshCw className={styles.spinning} size={14} /> : null}
                {pendingStatus === "ACTIVE" ? "활성화" : "비활성화"}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
