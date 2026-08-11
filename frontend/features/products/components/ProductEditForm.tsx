"use client";

import { useMemo, useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, CheckCircle2, Pencil } from "lucide-react";
import {
  useGetCategoriesQuery,
  useGetProductByIdQuery,
  useGetSubCategoriesQuery,
  useModifyProductMutation,
} from "../api/productsApi";
import { queryErrorMessage } from "@/features/store/api/queryError";
import styles from "./ProductEditForm.module.css";

interface ProductEditFormProps {
  productId: string;
}

function emptyForm() {
  return { categoryId: "", subCategoryId: "", modelName: "", modelCode: "", memo: "" };
}

export function ProductEditForm({ productId }: ProductEditFormProps) {
  const router = useRouter();
  const [form, setForm] = useState(emptyForm);
  const [prefilledFor, setPrefilledFor] = useState<string | null>(null);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const { data: product, isFetching: isProductLoading, error: productError } = useGetProductByIdQuery(productId);
  const { data: categories, isLoading: categoriesLoading, error: categoriesError } = useGetCategoriesQuery();
  const { data: subCategories, isLoading: subCategoriesLoading, error: subCategoriesError } = useGetSubCategoriesQuery();
  const [modifyProduct, modifyState] = useModifyProductMutation();

  // 제품·서브카테고리 조회가 끝나면 폼을 한 번만 채운다 — subCategoryId로 categoryId를 역으로 찾아야 해서
  // 두 쿼리가 다 끝난 뒤에 채운다. 렌더 중에 바로 보정해서(state adjust) 그 뒤로 사용자가 직접
  // 고치는 값을 덮어쓰지 않는다 — useEffect로 하면 리렌더가 한 번 더 걸린다.
  const isPrefilled = prefilledFor === productId;
  if (!isPrefilled && product && subCategories) {
    const matchedSubCategory = subCategories.find((sub) => sub.id === String(product.subCategoryId));
    setForm({
      categoryId: matchedSubCategory?.categoryId ?? String(product.categoryId),
      subCategoryId: String(product.subCategoryId),
      modelName: product.modelName,
      modelCode: product.modelCode,
      memo: product.memo,
    });
    setPrefilledFor(productId);
  }

  const availableSubCategories = useMemo(
    () => (subCategories ?? []).filter((sub) => sub.categoryId === form.categoryId),
    [subCategories, form.categoryId],
  );

  const loadErrorMessage = productError
    ? queryErrorMessage(productError, "제품 정보를 불러오지 못했습니다.")
    : categoriesError
      ? queryErrorMessage(categoriesError, "카테고리 목록을 불러오지 못했습니다.")
      : subCategoriesError
        ? queryErrorMessage(subCategoriesError, "서브카테고리 목록을 불러오지 못했습니다.")
        : null;

  const submitErrorMessage = modifyState.isError
    ? queryErrorMessage(modifyState.error, "제품을 수정하지 못했습니다.")
    : null;

  const handleCategoryChange = (categoryId: string) => {
    setForm((current) => ({ ...current, categoryId, subCategoryId: "" }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!form.subCategoryId) return;

    try {
      await modifyProduct({
        id: productId,
        request: {
          subCategoryId: Number(form.subCategoryId),
          modelName: form.modelName.trim(),
          modelCode: form.modelCode.trim(),
          memo: form.memo.trim(),
        },
      }).unwrap();
      setShowSuccessModal(true);
    } catch {
      // 에러 메시지는 modifyState.error 에서 표시한다.
    }
  };

  const isLoading = isProductLoading || categoriesLoading || subCategoriesLoading;

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><Pencil size={15} /> Products</p>
          <h1>제품 수정</h1>
          <p>카테고리와 서브카테고리를 선택하고 모델 정보를 고쳐서 저장합니다.</p>
        </div>
        <Link href="/admin/products" className={styles.backLink}>
          <ArrowLeft size={15} /> 목록으로
        </Link>
      </header>

      {loadErrorMessage ? <div className={styles.error}>{loadErrorMessage}</div> : null}

      <article className={styles.panel}>
        {isLoading && !isPrefilled ? (
          <div className={styles.state}>불러오는 중...</div>
        ) : (
          <form className={styles.form} onSubmit={handleSubmit}>
            <div className={styles.grid}>
              <label className={styles.field}>
                <span>카테고리</span>
                <select
                  required
                  value={form.categoryId}
                  onChange={(event) => handleCategoryChange(event.target.value)}
                >
                  <option value="" disabled>카테고리 선택</option>
                  {(categories ?? []).map((category) => (
                    <option key={category.id} value={category.id}>{category.name}</option>
                  ))}
                </select>
              </label>

              <label className={styles.field}>
                <span>서브카테고리</span>
                <select
                  required
                  value={form.subCategoryId}
                  onChange={(event) => setForm((current) => ({ ...current, subCategoryId: event.target.value }))}
                  disabled={!form.categoryId}
                >
                  <option value="" disabled>서브카테고리 선택</option>
                  {availableSubCategories.map((sub) => (
                    <option key={sub.id} value={sub.id}>{sub.name}</option>
                  ))}
                </select>
              </label>

              <label className={styles.field}>
                <span>모델명</span>
                <input
                  required
                  type="text"
                  value={form.modelName}
                  onChange={(event) => setForm((current) => ({ ...current, modelName: event.target.value }))}
                  placeholder="예: 김치냉장고 프리미엄"
                />
              </label>

              <label className={styles.field}>
                <span>모델코드</span>
                <input
                  required
                  type="text"
                  value={form.modelCode}
                  onChange={(event) => setForm((current) => ({ ...current, modelCode: event.target.value }))}
                  placeholder="예: KC-2024-PRM"
                />
              </label>
            </div>

            <label className={styles.field}>
              <span>메모</span>
              <textarea
                value={form.memo}
                onChange={(event) => setForm((current) => ({ ...current, memo: event.target.value }))}
                placeholder="선택 입력"
                rows={3}
              />
            </label>

            {submitErrorMessage ? <p className={styles.error}>{submitErrorMessage}</p> : null}

            <button type="submit" className={styles.submit} disabled={modifyState.isLoading}>
              {modifyState.isLoading ? "저장 중..." : "저장"}
            </button>
          </form>
        )}
      </article>

      {showSuccessModal ? (
        <div className={styles.backdrop} onClick={() => router.push("/admin/products")}>
          <div className={styles.modal} onClick={(event) => event.stopPropagation()}>
            <div className={styles.modalIcon}>
              <CheckCircle2 size={28} />
            </div>
            <p className={styles.modalMessage}>제품을 수정했습니다.</p>
            <button
              type="button"
              className={styles.modalConfirm}
              onClick={() => router.push("/admin/products")}
              autoFocus
            >
              확인
            </button>
          </div>
        </div>
      ) : null}
    </section>
  );
}
