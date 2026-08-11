"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, FilePlus2 } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { CustomerInfoForm } from "@/features/customers/components/CustomerInfoForm";
import type { CustomerInfoFormValue } from "@/features/customers/types/customer";
import { useCreateServiceRequestMutation, useGetBaseFeeQuery } from "../../api/serviceRequestApi";
import { emptyProductFieldsValue, isProductFieldsValid, toProductSelection } from "../ServiceRequestForm/productFieldsUtils";
import { ServiceRequestFormFields, type ServiceRequestFormValues } from "../ServiceRequestForm/ServiceRequestFormFields";
import styles from "./ServiceRequestCreateForm.module.css";

function initialForm(): ServiceRequestFormValues {
  return { product: emptyProductFieldsValue(), symptom: "", remarks: "" };
}

function emptyCustomerInfo(): CustomerInfoFormValue {
  return { phoneNumber: "", name: "", zipCode: "", address: "", detailAddress: "", memo: "" };
}

function isCustomerInfoValid(value: CustomerInfoFormValue): boolean {
  return value.name.trim() !== "" && value.phoneNumber.trim() !== ""
    && value.zipCode.trim() !== "" && value.address.trim() !== "";
}

export function ServiceRequestCreateForm() {
  const router = useRouter();
  const [form, setForm] = useState(initialForm);
  const [customerInfo, setCustomerInfo] = useState(emptyCustomerInfo);
  const [resetKey, setResetKey] = useState(0);
  const [createServiceRequest, createState] = useCreateServiceRequestMutation();
  const { data: baseFee } = useGetBaseFeeQuery();

  const submitErrorMessage = createState.isError
    ? queryErrorMessage(createState.error, "접수를 생성하지 못했습니다.")
    : null;

  const canSubmit = isCustomerInfoValid(customerInfo) && isProductFieldsValid(form.product) && form.symptom.trim() !== "";

  const handleReset = () => {
    setForm(initialForm());
    setCustomerInfo(emptyCustomerInfo());
    setResetKey((key) => key + 1);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canSubmit) return;

    try {
      const result = await createServiceRequest({
        customerName: customerInfo.name.trim(),
        customerPhoneNumber: customerInfo.phoneNumber.trim(),
        customerZipCode: customerInfo.zipCode.trim(),
        customerAddress: customerInfo.address.trim(),
        customerDetailAddress: customerInfo.detailAddress.trim(),
        customerMemo: customerInfo.memo.trim(),
        ...toProductSelection(form.product),
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
      <button type="button" className={styles.backButton} onClick={() => router.back()}>
        <ArrowLeft size={16} /> 뒤로가기
      </button>

      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><FilePlus2 size={15} /> Reception</p>
          <h1>접수 생성</h1>
        </div>
      </header>

      <form className={styles.form} onSubmit={handleSubmit}>
        <ServiceRequestFormFields
          form={form}
          onChange={(patch) => setForm((current) => ({ ...current, ...patch }))}
          customerContent={<CustomerInfoForm key={resetKey} onChange={setCustomerInfo} />}
          baseFeeContent={<p className={styles.hint}>{baseFee ? `${baseFee.baseFee.toLocaleString("ko-KR")}원` : "-"}</p>}
          resetKey={resetKey}
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