"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, MapPin, Pencil, Phone, RefreshCw, User, UserPlus, X } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { useCancelServiceRequestMutation, useGetServiceRequestDetailQuery } from "../../api/serviceRequestApi";
import { statusBadgeData, statusLabel } from "../serviceRequestStatusUtils";
import { ServiceRequestCancelModal } from "./ServiceRequestCancelModal";
import { formatDateTime, formatFee, formatVisit } from "./serviceRequestDetailUtils";
import styles from "./ServiceRequestDetail.module.css";

interface ServiceRequestDetailProps {
  id: string;
}

export function ServiceRequestDetail({ id }: ServiceRequestDetailProps) {
  const router = useRouter();
  const { data, isFetching, error } = useGetServiceRequestDetailQuery(id);
  const [cancelServiceRequest, cancelState] = useCancelServiceRequestMutation();
  const [showCancelModal, setShowCancelModal] = useState(false);

  const errorMessage = error ? queryErrorMessage(error, "접수 상세를 불러오지 못했습니다.") : null;
  const cancelErrorMessage = cancelState.isError
    ? queryErrorMessage(cancelState.error, "접수를 취소하지 못했습니다.")
    : null;

  const handleConfirmCancel = async (cancelReason: string) => {
    try {
      await cancelServiceRequest({ id, request: { cancelReason } }).unwrap();
      setShowCancelModal(false);
    } catch {
      // 에러 메시지는 cancelState.error 에서 표시한다.
    }
  };

  if (isFetching && !data) {
    return <div className={styles.stateMessage}>불러오는 중...</div>;
  }
  if (errorMessage || !data) {
    return <div className={styles.stateMessage}>{errorMessage ?? "접수 정보를 찾을 수 없습니다."}</div>;
  }

  const isCancelled = data.status === "CANCELLED";

  const handleAssign = () => {
    const params = new URLSearchParams({ requestNumber: data.requestNumber, mode: "assign" });
    router.push(`/counselor/reception/new/${id}/assign?${params}`);
  };

  const handleReassign = () => {
    const params = new URLSearchParams({
      requestNumber: data.requestNumber,
      mode: "reassign",
      version: String(data.version),
    });
    router.push(`/counselor/reception/new/${id}/assign?${params}`);
  };

  return (
    <div className={styles.page}>
      <button type="button" className={styles.backButton} onClick={() => router.back()}>
        <ArrowLeft size={16} /> 뒤로가기
      </button>

      <article className={styles.card}>
        <header className={styles.header}>
          <div>
            <p className={styles.requestNumberTitle}>{data.requestNumber}</p>
            <div className={styles.titleRow}>
              <h1>{data.customerName}님의 A/S 접수</h1>
              <span className={styles.statusBadge} data-status={statusBadgeData(data.status)}>
                <span className={styles.statusDot} />
                {statusLabel(data.status)}
              </span>
            </div>
          </div>

          <div className={styles.headerActions}>
            <p className={styles.requestedAtText}>접수일 {formatDateTime(data.requestedAt)}</p>
            {!isCancelled ? (
              <div className={styles.actionButtons}>
                <button type="button" className={styles.outlineButton} onClick={() => router.push(`/counselor/reception/${id}/edit`)}>
                  <Pencil size={14} /> 정보 수정
                </button>
                <button type="button" className={styles.dangerButton} onClick={() => setShowCancelModal(true)}>
                  <X size={14} /> 접수 취소
                </button>
              </div>
            ) : null}
          </div>
        </header>

        <div className={styles.receptionistSection}>
          <p className={styles.sectionTitle}>접수자</p>
          <p className={styles.receptionistValue}>{data.receptionistName ?? "-"}</p>
        </div>

        <div className={styles.infoGrid}>
          <div className={styles.infoColumn}>
            <p className={styles.sectionTitle}>고객 정보</p>
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
          </div>
          <div className={styles.infoColumn}>
            <p className={styles.sectionTitle}>제품 정보</p>
            <div className={styles.field}>
              <div className={styles.fieldLabel}>제품</div>
              <div className={styles.fieldValue}>{data.modelName ?? "-"}</div>
            </div>
            {data.subCategoryName ? (
              <div className={styles.field}>
                <div className={styles.fieldLabel}>서브카테고리</div>
                <div className={styles.fieldValue}>{data.subCategoryName}</div>
              </div>
            ) : null}
            <div className={styles.field}>
              <div className={styles.fieldLabel}>특이사항</div>
              <div className={styles.fieldValue}>{data.remarks || "-"}</div>
            </div>
            <div className={styles.field}>
              <div className={styles.fieldLabel}>출장비</div>
              <div className={styles.fieldValue}>{formatFee(data.baseFee)}</div>
            </div>
          </div>
        </div>

        <div className={styles.symptomSection}>
          <p className={styles.sectionTitle}>고장 증상</p>
          <p className={styles.symptomText}>{data.symptom || "-"}</p>
        </div>

        <div className={styles.footerSection}>
          {isCancelled ? (
            <div>
              <p className={styles.sectionTitle}>취소 정보</p>
              <div className={styles.cancelInfo}>
                <div className={styles.field}>
                  <div className={styles.fieldLabel}>취소 일시</div>
                  <div className={styles.fieldValue}>{formatDateTime(data.cancelledAt) ?? "-"}</div>
                </div>
                <div className={styles.field}>
                  <div className={styles.fieldLabel}>취소 사유</div>
                  <div className={styles.fieldValue}>{data.cancelReason || "-"}</div>
                </div>
              </div>
            </div>
          ) : data.technicianName ? (
            <div className={styles.technicianRow}>
              <div className={styles.technicianInfo}>
                <span className={styles.avatar}>{data.technicianName.charAt(0)}</span>
                <div>
                  <p className={styles.fieldLabel}>방문 예약</p>
                  <p className={styles.fieldValue}>
                    {data.technicianName} 기사 · {formatVisit(data.visitDate, data.visitStartTime, data.visitEndTime)}
                  </p>
                </div>
              </div>
              <button type="button" className={styles.outlineButton} onClick={handleReassign}>
                <RefreshCw size={14} /> 재배정
              </button>
            </div>
          ) : (
            <div className={styles.emptyTechnician}>
              <div>
                <span className={styles.emptyAvatar}><User size={16} /></span>
                <p>아직 기사님이 배정되지 않았습니다.</p>
              </div>
              <button type="button" className={styles.outlineButton} onClick={handleAssign}>
                <UserPlus size={14} /> 배정
              </button>
            </div>
          )}
        </div>
      </article>

      {showCancelModal ? (
        <ServiceRequestCancelModal
          isSubmitting={cancelState.isLoading}
          errorMessage={cancelErrorMessage}
          onConfirm={handleConfirmCancel}
          onClose={() => setShowCancelModal(false)}
        />
      ) : null}
    </div>
  );
}