"use client";

import { CheckCircle2 } from "lucide-react";
import type { AssignServiceRequestResponse } from "../../types/serviceRequest";
import styles from "./TechnicianAssignForm.module.css";

function toMinutePrefix(time: string): string {
  return time.slice(0, 5);
}

interface AssignmentCompletePanelProps {
  result: AssignServiceRequestResponse;
  requestNumber: string | null;
  baseFee: number | null;
  title?: string;
  onConfirm: () => void;
}

export function AssignmentCompletePanel({ result, requestNumber, baseFee, title = "기사 배정이 완료됐습니다", onConfirm }: AssignmentCompletePanelProps) {
  return (
    <section className={styles.completeStage}>
      <article className={styles.completePanel}>
        <div className={styles.completeIcon}>
          <CheckCircle2 size={28} />
        </div>
        <h1>{title}</h1>
        <dl className={styles.completeSummary}>
          {requestNumber ? (
            <div>
              <dt>접수번호</dt>
              <dd>{requestNumber}</dd>
            </div>
          ) : null}
          <div>
            <dt>담당 기사</dt>
            <dd>{result.technicianName}</dd>
          </div>
          <div>
            <dt>방문 예정 일시</dt>
            <dd>{result.visitDate} {toMinutePrefix(result.visitStartTime)}~{toMinutePrefix(result.visitEndTime)}</dd>
          </div>
          {baseFee !== null ? (
            <div>
              <dt>출장비</dt>
              <dd>{baseFee.toLocaleString("ko-KR")}원</dd>
            </div>
          ) : null}
        </dl>
        <button type="button" className={styles.completeButton} onClick={onConfirm}>
          접수 목록으로
        </button>
      </article>
    </section>
  );
}