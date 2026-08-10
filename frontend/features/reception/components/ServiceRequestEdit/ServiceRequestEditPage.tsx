"use client";

import { useGetSubCategoriesQuery } from "@/features/products/api/productsApi";
import { useGetServiceRequestDetailQuery } from "../../api/serviceRequestApi";
import type { ProductFieldsValue } from "../ServiceRequestForm/productFieldsUtils";
import { ServiceRequestEditForm } from "./ServiceRequestEditForm";
import styles from "../ServiceRequestCreateForm.module.css";

interface ServiceRequestEditPageProps {
  id: string;
}

export function ServiceRequestEditPage({ id }: ServiceRequestEditPageProps) {
  const { data, isFetching, error } = useGetServiceRequestDetailQuery(id);
  // TODO: selectedSubCategoryId로 부모 categoryId를 찾기 위해 필요 - 추후 교체
  const { data: subCategories, isFetching: isSubCategoriesFetching } = useGetSubCategoriesQuery();

  if ((isFetching && !data) || (isSubCategoriesFetching && !subCategories)) {
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
        categoryId: subCategories?.find((sub) => sub.id === data.selectedSubCategoryId)?.categoryId ?? "",
        subCategoryId: data.selectedSubCategoryId ?? "",
        modelName: data.modelName ?? "",
        modelCode: "",
        matchedProductId: null,
      };

  return (
    <ServiceRequestEditForm
      serviceRequestId={id}
      initialValues={{
        customerId: data.customerId,
        product,
        symptom: data.symptom,
        remarks: data.remarks,
      }}
      baseFee={data.baseFee}
      version={data.version}
    />
  );
}