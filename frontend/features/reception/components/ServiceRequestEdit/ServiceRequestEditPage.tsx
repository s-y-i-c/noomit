"use client";

import { useGetCustomerByIdQuery } from "@/features/customers/api/customersApi";
import { useGetServiceRequestDetailQuery } from "../../api/serviceRequestApi";
import type { ProductFieldsValue } from "../ServiceRequestForm/productFieldsUtils";
import { ServiceRequestEditForm } from "./ServiceRequestEditForm";
import styles from "../ServiceRequestCreate/ServiceRequestCreateForm.module.css";

interface ServiceRequestEditPageProps {
  id: string;
}

export function ServiceRequestEditPage({ id }: ServiceRequestEditPageProps) {
  const { data, isFetching, error } = useGetServiceRequestDetailQuery(id);
  const { data: customer, isFetching: isCustomerFetching } = useGetCustomerByIdQuery(
    data?.customerId ?? "", { skip: !data?.customerId },
  );

  if ((isFetching && !data) || (isCustomerFetching && !customer)) {
    return <div className={styles.stateMessage}>불러오는 중...</div>;
  }
  if (error || !data) {
    return <div className={styles.stateMessage}>접수 정보를 찾을 수 없습니다.</div>;
  }

  const product: ProductFieldsValue = data.productId
    ? {
        categoryId: "",
        subCategoryId: "",
        modelName: data.modelName ?? "",
        modelCode: "",
        matchedProductId: data.productId,
      }
    : {
        categoryId: data.selectedCategoryId ?? "",
        subCategoryId: data.selectedSubCategoryId ?? "",
        modelName: data.modelName ?? "",
        modelCode: "",
        matchedProductId: null,
      };

  return (
    <ServiceRequestEditForm
      serviceRequestId={id}
      initialCustomer={customer ?? null}
      initialValues={{
        product,
        symptom: data.symptom,
        remarks: data.remarks,
      }}
      baseFee={data.baseFee}
      version={data.version}
    />
  );
}