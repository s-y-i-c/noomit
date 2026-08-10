"use client";

import { useGetServiceRequestDetailQuery } from "../api/serviceRequestApi";
import { ServiceRequestEditForm } from "./ServiceRequestEditForm";
import styles from "./ServiceRequestCreateForm.module.css";

interface ServiceRequestEditPageProps {
  id: string;
}

export function ServiceRequestEditPage({ id }: ServiceRequestEditPageProps) {
  const { data, isFetching, error } = useGetServiceRequestDetailQuery(id);

  if (isFetching && !data) {
    return <div className={styles.stateMessage}>불러오는 중...</div>;
  }
  if (error || !data) {
    return <div className={styles.stateMessage}>접수 정보를 찾을 수 없습니다.</div>;
  }

  return (
    <ServiceRequestEditForm
      serviceRequestId={id}
      initialValues={{
        customerId: data.customerId,
        productId: data.productId,
        symptom: data.symptom,
        remarks: data.remarks,
      }}
      baseFee={data.baseFee}
    />
  );
}