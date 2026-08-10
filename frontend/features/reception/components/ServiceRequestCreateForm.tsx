"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { FilePlus2 } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { useCreateServiceRequestMutation } from "../api/serviceRequestApi";
import { ServiceRequestFormFields, type ServiceRequestFormValues } from "./ServiceRequestFormFields";
import styles from "./ServiceRequestCreateForm.module.css";

function initialForm(): ServiceRequestFormValues {
  return { customerId: "", productId: "", symptom: "", remarks: "" };
}

export function ServiceRequestCreateForm() {
  const router = useRouter();
  const [form, setForm] = useState(initialForm);
  const [createServiceRequest, createState] = useCreateServiceRequestMutation();

  const submitErrorMessage = createState.isError
    ? queryErrorMessage(createState.error, "접수를 생성하지 못했습니다.")
    : null;

  const canSubmit = form.customerId !== "" && form.productId !== "" && form.symptom.trim() !== "";

  const handleReset = () => setForm(initialForm());

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canSubmit) return;

    try {
      const result = await createServiceRequest({
        customerId: form.customerId,
        productId: form.productId,
        symptom: form.symptom.trim(),
        remarks: form.remarks.trim(),
      }).unwrap();
      router.push(`/counselor/reception/new/${result.id}/assign?requestNumber=${encodeURIComponent(result.requestNumber)}`);
    } catch {
      // 에러 메시지는 createState.error 에서 표시
    }
  };

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><FilePlus2 size={15} /> Reception</p>
          <h1>접수 생성</h1>
          <p>고객·제품 정보와 접수 상세 내용을 입력합니다.</p>
        </div>
      </header>

      <form className={styles.form} onSubmit={handleSubmit}>
        <ServiceRequestFormFields
          form={form}
          onChange={(patch) => setForm((current) => ({ ...current, ...patch }))}
          baseFeeContent={<p className={styles.hint}>출장비는 접수 완료 후 확인됩니다.</p>}
        />

        {submitErrorMessage ? <p className={styles.error}>{submitErrorMessage}</p> : null}

        <div className={styles.actions}>
          <button type="button" className={styles.resetButton} onClick={handleReset}>초기화</button>
          <button type="submit" className={styles.submitButton} disabled={!canSubmit || createState.isLoading}>
            {createState.isLoading ? "생성 중..." : "다음: 기사 배정 →"}
          </button>
        </div>
      </form>
    </section>
  );
}