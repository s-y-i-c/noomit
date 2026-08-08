"use client";

import { RefreshCw, X } from "lucide-react";
import { useChangeCustomerStatusMutation, useGetCustomerByIdQuery } from "../api/customersApi";
import styles from "./CustomerDetailModal.module.css";

interface CustomerDetailModalProps {
  customerId: string;
  onClose: () => void;
}

export function CustomerDetailModal({ customerId, onClose }: CustomerDetailModalProps) {
  const { data: customer, isFetching, error } = useGetCustomerByIdQuery(customerId);
  const [changeStatus, { isLoading: isChangingStatus, error: changeStatusError }] =
    useChangeCustomerStatusMutation();

  const errorMessage = typeof error === "object" && error !== null && "message" in error
    ? String(error.message)
    : null;
  const changeStatusErrorMessage =
    typeof changeStatusError === "object" && changeStatusError !== null && "message" in changeStatusError
      ? String(changeStatusError.message)
      : null;

  const toggleStatus = () => {
    if (!customer) return;
    const nextStatus = customer.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    void changeStatus({ id: customer.id, status: nextStatus });
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
                    onClick={toggleStatus}
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
            {changeStatusErrorMessage ? <div className={styles.state}>{changeStatusErrorMessage}</div> : null}
          </>
        ) : null}
      </div>
    </div>
  );
}
