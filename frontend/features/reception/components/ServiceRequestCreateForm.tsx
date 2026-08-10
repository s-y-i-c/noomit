"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { FilePlus2 } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { useCreateServiceRequestMutation } from "../api/serviceRequestApi";
import { CustomerSelectorPlaceholder } from "./CustomerSelectorPlaceholder";
import { ProductSelectorPlaceholder } from "./ProductSelectorPlaceholder";
import styles from "./ServiceRequestCreateForm.module.css";

function initialForm() {
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
        <article className={styles.panel}>
          <h2 className={styles.sectionTitle}>고객 정보</h2>
          <CustomerSelectorPlaceholder
            onSelect={(customer) => setForm((current) => ({ ...current, customerId: customer.customerId }))}
          />
          {form.customerId ? <p className={styles.selectedHint}>선택된 고객 ID: {form.customerId}</p> : null}
        </article>

        <article className={styles.panel}>
          <h2 className={styles.sectionTitle}>제품 정보</h2>
          <ProductSelectorPlaceholder
            onSelect={(product) => setForm((current) => ({ ...current, productId: product.productId }))}
          />
          {form.productId ? <p className={styles.selectedHint}>선택된 제품 ID: {form.productId}</p> : null}
        </article>

        <article className={styles.panel}>
          <h2 className={styles.sectionTitle}>접수 상세 정보</h2>
          <label className={styles.field}>
            <span>고장 증상 *</span>
            <textarea
              required
              rows={3}
              value={form.symptom}
              onChange={(event) => setForm((current) => ({ ...current, symptom: event.target.value }))}
              placeholder="고객이 설명한 증상을 입력하세요"
            />
          </label>
          <label className={styles.field}>
            <span>특이사항</span>
            <textarea
              rows={3}
              value={form.remarks}
              onChange={(event) => setForm((current) => ({ ...current, remarks: event.target.value }))}
              placeholder="선택 입력"
            />
          </label>
        </article>

        <article className={styles.panel}>
          <h2 className={styles.sectionTitle}>출장비 정보</h2>
          <p className={styles.hint}>출장비는 접수 완료 후 확인됩니다.</p>
        </article>

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