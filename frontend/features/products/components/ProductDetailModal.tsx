"use client";

import { RefreshCw, X } from "lucide-react";
import {
  useChangeProductStatusMutation,
  useGetCategoriesQuery,
  useGetProductByIdQuery,
  useGetSubCategoriesQuery,
} from "../api/productsApi";
import styles from "./ProductDetailModal.module.css";

interface ProductDetailModalProps {
  productId: string;
  onClose: () => void;
  /** true면 상태를 배지로만 보여주고 변경할 수 없다 (기사 등 조회 전용 화면용). */
  readOnly?: boolean;
}

export function ProductDetailModal({ productId, onClose, readOnly = false }: ProductDetailModalProps) {
  const { data: product, isFetching, error } = useGetProductByIdQuery(productId);
  const { data: categories } = useGetCategoriesQuery();
  const { data: subCategories } = useGetSubCategoriesQuery();
  const [changeStatus, { isLoading: isChangingStatus, error: changeStatusError }] =
    useChangeProductStatusMutation();

  const errorMessage = typeof error === "object" && error !== null && "message" in error
    ? String(error.message)
    : null;
  const changeStatusErrorMessage =
    typeof changeStatusError === "object" && changeStatusError !== null && "message" in changeStatusError
      ? String(changeStatusError.message)
      : null;

  const toggleStatus = () => {
    if (!product) return;
    const nextStatus = product.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    void changeStatus({ id: product.id, status: nextStatus });
  };

  const categoryName = categories?.find((category) => category.id === String(product?.categoryId))?.name ?? "-";
  const subCategoryName = subCategories?.find((sub) => sub.id === String(product?.subCategoryId))?.name ?? "-";

  return (
    <div className={styles.backdrop} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2>제품 상세 정보</h2>
          <button type="button" onClick={onClose} aria-label="닫기" className={styles.closeButton}>
            <X size={18} />
          </button>
        </div>

        {isFetching ? <div className={styles.state}>불러오는 중...</div> : null}
        {errorMessage ? <div className={styles.state}>{errorMessage}</div> : null}

        {product ? (
          <>
            <dl className={styles.fields}>
              <div className={styles.field}>
                <dt>모델명</dt>
                <dd>{product.modelName}</dd>
              </div>
              <div className={styles.field}>
                <dt>모델코드</dt>
                <dd>{product.modelCode}</dd>
              </div>
              <div className={styles.field}>
                <dt>상태</dt>
                <dd>
                  {readOnly ? (
                    <span className={styles.statusBadge} data-status={product.status}>
                      {product.status === "ACTIVE" ? "판매중" : "단종"}
                    </span>
                  ) : (
                    <button
                      type="button"
                      className={styles.statusButton}
                      data-status={product.status}
                      onClick={toggleStatus}
                      disabled={isChangingStatus}
                    >
                      {isChangingStatus ? (
                        <RefreshCw className={styles.spinning} size={12} />
                      ) : null}
                      {product.status === "ACTIVE" ? "판매중" : "단종"}
                    </button>
                  )}
                </dd>
              </div>
              <div className={styles.field}>
                <dt>카테고리</dt>
                <dd>{categoryName}</dd>
              </div>
              <div className={styles.field}>
                <dt>서브카테고리</dt>
                <dd>{subCategoryName}</dd>
              </div>
              <div className={styles.field}>
                <dt>메모</dt>
                <dd>{product.memo || "-"}</dd>
              </div>
              <div className={styles.field}>
                <dt>제품 ID</dt>
                <dd>{product.id}</dd>
              </div>
            </dl>
            {changeStatusErrorMessage ? <div className={styles.state}>{changeStatusErrorMessage}</div> : null}
          </>
        ) : null}
      </div>
    </div>
  );
}
