"use client";

import type { ReactNode } from "react";
import { CustomerSelectorPlaceholder } from "../ServiceRequestEdit/CustomerSelectorPlaceholder";
import { ProductFields } from "./ProductFields";
import type { ProductFieldsValue } from "./productFieldsUtils";
import styles from "../ServiceRequestCreateForm.module.css";

export interface ServiceRequestFormValues {
  customerId: string;
  product: ProductFieldsValue;
  symptom: string;
  remarks: string;
}

interface ServiceRequestFormFieldsProps {
  form: ServiceRequestFormValues;
  onChange: (patch: Partial<ServiceRequestFormValues>) => void;
  baseFeeContent: ReactNode;
}

// 고객/제품 정보 · 접수 상세 · 출장비 섹션 — 생성/수정 폼이 공유
export function ServiceRequestFormFields({ form, onChange, baseFeeContent }: ServiceRequestFormFieldsProps) {
  return (
    <>
      <article className={styles.panel}>
        <h2 className={styles.sectionTitle}>고객 정보</h2>
        <CustomerSelectorPlaceholder
          initialValue={form.customerId}
          onSelect={(customer) => onChange({ customerId: customer.customerId })}
        />
        {form.customerId ? <p className={styles.selectedHint}>선택된 고객 ID: {form.customerId}</p> : null}
      </article>

      <article className={styles.panel}>
        <h2 className={styles.sectionTitle}>제품 정보</h2>
        <ProductFields
          initialValue={form.product}
          onChange={(product) => onChange({ product })}
        />
      </article>

      <article className={styles.panel}>
        <h2 className={styles.sectionTitle}>접수 상세 정보</h2>
        <label className={styles.field}>
          <span>고장 증상 *</span>
          <textarea
            required
            rows={3}
            value={form.symptom}
            onChange={(event) => onChange({ symptom: event.target.value })}
            placeholder="고객이 설명한 증상을 입력하세요"
          />
        </label>
        <label className={styles.field}>
          <span>특이사항</span>
          <textarea
            rows={3}
            value={form.remarks}
            onChange={(event) => onChange({ remarks: event.target.value })}
            placeholder="선택 입력"
          />
        </label>
      </article>

      <article className={styles.panel}>
        <h2 className={styles.sectionTitle}>출장비 정보</h2>
        {baseFeeContent}
      </article>
    </>
  );
}