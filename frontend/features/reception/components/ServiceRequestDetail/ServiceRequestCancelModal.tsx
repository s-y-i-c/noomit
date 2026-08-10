"use client";

import { useState } from "react";
import styles from "./ServiceRequestDetail.module.css";

interface ServiceRequestCancelModalProps {
  isSubmitting: boolean;
  errorMessage: string | null;
  onConfirm: (cancelReason: string) => void;
  onClose: () => void;
}

export function ServiceRequestCancelModal({ isSubmitting, errorMessage, onConfirm, onClose }: ServiceRequestCancelModalProps) {
  const [cancelReason, setCancelReason] = useState("");

  return (
    <div className={styles.backdrop} onClick={onClose}>
      <div className={styles.modal} onClick={(event) => event.stopPropagation()}>
        <h2>접수를 취소할까요?</h2>
        <label className={styles.modalField}>
          <span>취소 사유 (선택)</span>
          <textarea
            rows={3}
            value={cancelReason}
            onChange={(event) => setCancelReason(event.target.value)}
            placeholder="사유를 입력하세요"
          />
        </label>
        {errorMessage ? <p className={styles.error}>{errorMessage}</p> : null}
        <div className={styles.modalActions}>
          <button type="button" className={styles.outlineButton} onClick={onClose}>
            닫기
          </button>
          <button
            type="button"
            className={styles.dangerButton}
            disabled={isSubmitting}
            onClick={() => onConfirm(cancelReason.trim())}
          >
            {isSubmitting ? "취소 처리 중..." : "취소 확정"}
          </button>
        </div>
      </div>
    </div>
  );
}