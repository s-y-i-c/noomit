"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, Pencil } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { CustomerInfoForm } from "@/features/customers/components/CustomerInfoForm";
import type { Customer, CustomerInfoFormValue } from "@/features/customers/types/customer";
import { useUpdateServiceRequestMutation } from "../../api/serviceRequestApi";
import { isProductFieldsValid, toProductSelection } from "../ServiceRequestForm/productFieldsUtils";
import { ServiceRequestFormFields, type ServiceRequestFormValues } from "../ServiceRequestForm/ServiceRequestFormFields";
import styles from "../ServiceRequestCreate/ServiceRequestCreateForm.module.css";

function toCustomerInfoValue(customer: Customer | null): CustomerInfoFormValue {
  return {
    phoneNumber: customer?.phoneNumber ?? "",
    name: customer?.name ?? "",
    zipCode: customer?.zipCode ?? "",
    address: customer?.address ?? "",
    detailAddress: customer?.detailAddress ?? "",
    memo: customer?.memo ?? "",
  };
}

function isCustomerInfoValid(value: CustomerInfoFormValue): boolean {
  return value.name.trim() !== "" && value.phoneNumber.trim() !== ""
    && value.zipCode.trim() !== "" && value.address.trim() !== "";
}

interface ServiceRequestEditFormProps {
  serviceRequestId: string;
  initialCustomer: Customer | null;
  initialValues: ServiceRequestFormValues;
  baseFee: number;
  version: number;
}

export function ServiceRequestEditForm({ serviceRequestId, initialCustomer, initialValues, baseFee, version }: ServiceRequestEditFormProps) {
  const router = useRouter();
  const [form, setForm] = useState<ServiceRequestFormValues>(initialValues);
  const [customerInfo, setCustomerInfo] = useState(() => toCustomerInfoValue(initialCustomer));
  const [resetKey, setResetKey] = useState(0);
  const [updateServiceRequest, updateState] = useUpdateServiceRequestMutation();

  const submitErrorMessage = updateState.isError
    ? queryErrorMessage(updateState.error, "접수 정보를 수정하지 못했습니다.")
    : null;

  const canSubmit = isCustomerInfoValid(customerInfo) && isProductFieldsValid(form.product) && form.symptom.trim() !== "";

  const handleReset = () => {
    setForm(initialValues);
    setCustomerInfo(toCustomerInfoValue(initialCustomer));
    setResetKey((key) => key + 1);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canSubmit) return;

    try {
      await updateServiceRequest({
        id: serviceRequestId,
        request: {
          customerName: customerInfo.name.trim(),
          customerPhoneNumber: customerInfo.phoneNumber.trim(),
          customerZipCode: customerInfo.zipCode.trim(),
          customerAddress: customerInfo.address.trim(),
          customerDetailAddress: customerInfo.detailAddress.trim(),
          customerMemo: customerInfo.memo.trim(),
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
      <button type="button" className={styles.backButton} onClick={() => router.back()}>
        <ArrowLeft size={16} /> 뒤로가기
      </button>

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
          customerContent={<CustomerInfoForm key={resetKey} initialCustomer={initialCustomer} onChange={setCustomerInfo} />}
          baseFeeContent={<p className={styles.hint}>{baseFee.toLocaleString("ko-KR")}원</p>}
          resetKey={resetKey}
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