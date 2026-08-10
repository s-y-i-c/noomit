"use client";

import { useRouter } from "next/navigation";
import { ArrowLeft, MapPin, Phone } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { useGetMyAssignedRequestDetailQuery } from "../api/assignedRequestApi";
import { toMinutePrefix } from "./dateUtils";
import styles from "./AssignedRequestDetail.module.css";

interface AssignedRequestDetailProps {
  id: string;
}

export function AssignedRequestDetail({ id }: AssignedRequestDetailProps) {
  const router = useRouter();
  const { data, isFetching, error } = useGetMyAssignedRequestDetailQuery(id);

  const errorMessage = error ? queryErrorMessage(error, "배정 상세를 불러오지 못했습니다.") : null;

  if (isFetching && !data) {
    return <div className={styles.stateMessage}>불러오는 중...</div>;
  }
  if (errorMessage || !data) {
    return <div className={styles.stateMessage}>{errorMessage ?? "배정 정보를 찾을 수 없습니다."}</div>;
  }

  return (
    <div className={styles.page}>
      <button type="button" className={styles.backButton} onClick={() => router.back()}>
        <ArrowLeft size={16} /> 뒤로가기
      </button>

      <article className={styles.card}>
        <header className={styles.header}>
          <p className={styles.requestNumberTitle}>{data.requestNumber}</p>
          <h1>{data.customerName}님 방문</h1>
          <p className={styles.visitText}>
            {data.visitDate} {toMinutePrefix(data.startTime)}~{toMinutePrefix(data.endTime)}
          </p>
        </header>

        <div className={styles.infoGrid}>
          <div className={styles.field}>
            <div className={styles.fieldLabel}>고객명</div>
            <div className={styles.fieldValue}>{data.customerName}</div>
          </div>
          <div className={styles.field}>
            <div className={styles.fieldLabel}>전화번호</div>
            <div className={styles.inlineValue}><Phone size={13} />{data.customerPhone}</div>
          </div>
          <div className={styles.field}>
            <div className={styles.fieldLabel}>주소</div>
            <div className={styles.inlineValue}>
              <MapPin size={13} />
              <span>{data.address}{data.detailAddress ? `, ${data.detailAddress}` : ""}</span>
            </div>
          </div>
          <div className={styles.field}>
            <div className={styles.fieldLabel}>제품</div>
            <div className={styles.fieldValue}>{data.modelName}</div>
          </div>
        </div>

        <div className={styles.symptomSection}>
          <p className={styles.sectionTitle}>고장 증상</p>
          <p className={styles.symptomText}>{data.symptom || "-"}</p>
        </div>

        {data.remarks ? (
          <div className={styles.symptomSection}>
            <p className={styles.sectionTitle}>특이사항</p>
            <p className={styles.symptomText}>{data.remarks}</p>
          </div>
        ) : null}
      </article>
    </div>
  );
}