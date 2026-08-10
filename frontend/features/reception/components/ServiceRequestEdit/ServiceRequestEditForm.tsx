"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { Pencil } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { useUpdateServiceRequestMutation } from "../../api/serviceRequestApi";
import { isProductFieldsValid, toProductSelection } from "../ServiceRequestForm/productFieldsUtils";
import { ServiceRequestFormFields, type ServiceRequestFormValues } from "../ServiceRequestForm/ServiceRequestFormFields";
import styles from "../ServiceRequestCreate/ServiceRequestCreateForm.module.css";

interface ServiceRequestEditFormProps {
  serviceRequestId: string;
  initialValues: ServiceRequestFormValues;
  baseFee: number;
  version: number;
}

export function ServiceRequestEditForm({ serviceRequestId, initialValues, baseFee, version }: ServiceRequestEditFormProps) {
  const router = useRouter();
  const [form, setForm] = useState<ServiceRequestFormValues>(initialValues);
  const [updateServiceRequest, updateState] = useUpdateServiceRequestMutation();

  const submitErrorMessage = updateState.isError
    ? queryErrorMessage(updateState.error, "접수 정보를 수정하지 못했습니다.")
    : null;

  const canSubmit = form.customerId !== "" && isProductFieldsValid(form.product) && form.symptom.trim() !== "";

  const handleReset = () => setForm(initialValues);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canSubmit) return;

    try {
      await updateServiceRequest({
        id: serviceRequestId,
        request: {
          customerId: form.customerId,
          ...toProductSelection(form.product),
          symptom: form.symptom.trim(),
          remarks: form.remarks.trim(),
          version,
        },
      }).unwrap();
      router.push(`/counselor/reception/${serviceRequestId}`);
    } catch {
      // 에러 메시지는 updateState.error 에서 표시
    }
  };

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><Pencil size={15} /> Reception</p>
          <h1>접수 수정</h1>
          <p>고객·제품 정보와 접수 상세 내용을 수정합니다.</p>
        </div>
      </header>

      <form className={styles.form} onSubmit={handleSubmit}>
        <ServiceRequestFormFields
          form={form}
          onChange={(patch) => setForm((current) => ({ ...current, ...patch }))}
          baseFeeContent={<p className={styles.hint}>{baseFee.toLocaleString("ko-KR")}원</p>}
        />

        {submitErrorMessage ? <p className={styles.error}>{submitErrorMessage}</p> : null}

        <div className={styles.actions}>
          <button type="button" className={styles.resetButton} onClick={handleReset}>초기화</button>
          <button type="submit" className={styles.submitButton} disabled={!canSubmit || updateState.isLoading}>
            {updateState.isLoading ? "수정 중..." : "수정"}
          </button>
        </div>
      </form>
    </section>
  );
}