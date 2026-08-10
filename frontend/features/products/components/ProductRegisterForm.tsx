"use client";

import { useMemo, useState, type FormEvent } from "react";
import Link from "next/link";
import { ArrowLeft, CheckCircle2, PackagePlus } from "lucide-react";
import { useGetCategoriesQuery, useGetSubCategoriesQuery, useRegisterProductMutation } from "../api/productsApi";
import { queryErrorMessage } from "@/features/store/api/queryError";
import styles from "./ProductRegisterForm.module.css";

function initialForm() {
  return { categoryId: "", subCategoryId: "", modelName: "", modelCode: "", memo: "" };
}

export function ProductRegisterForm() {
  const [form, setForm] = useState(initialForm);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const { data: categories, isLoading: categoriesLoading, error: categoriesError } = useGetCategoriesQuery();
  const { data: subCategories, isLoading: subCategoriesLoading, error: subCategoriesError } = useGetSubCategoriesQuery();
  const [registerProduct, registerState] = useRegisterProductMutation();

  const availableSubCategories = useMemo(
    () => (subCategories ?? []).filter((sub) => sub.categoryId === form.categoryId),
    [subCategories, form.categoryId],
  );

  const loadErrorMessage = categoriesError
    ? queryErrorMessage(categoriesError, "카테고리 목록을 불러오지 못했습니다.")
    : subCategoriesError
      ? queryErrorMessage(subCategoriesError, "서브카테고리 목록을 불러오지 못했습니다.")
      : null;

  const submitErrorMessage = registerState.isError
    ? queryErrorMessage(registerState.error, "제품을 등록하지 못했습니다.")
    : null;

  const handleCategoryChange = (categoryId: string) => {
    setForm((current) => ({ ...current, categoryId, subCategoryId: "" }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!form.subCategoryId) return;

    try {
      await registerProduct({
        subCategoryId: Number(form.subCategoryId),
        modelName: form.modelName.trim(),
        modelCode: form.modelCode.trim(),
        memo: form.memo.trim(),
      }).unwrap();
      setForm(initialForm());
      setShowSuccessModal(true);
    } catch {
      // 에러 메시지는 registerState.error 에서 표시한다.
    }
  };

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><PackagePlus size={15} /> Products</p>
          <h1>제품 등록</h1>
          <p>카테고리와 서브카테고리를 선택하고 모델 정보를 입력해 제품을 등록합니다.</p>
        </div>
        <Link href="/admin/products" className={styles.backLink}>
          <ArrowLeft size={15} /> 목록으로
        </Link>
      </header>

      {loadErrorMessage ? <div className={styles.error}>{loadErrorMessage}</div> : null}

      <article className={styles.panel}>
        <form className={styles.form} onSubmit={handleSubmit}>
          <div className={styles.grid}>
            <label className={styles.field}>
              <span>카테고리</span>
              <select
                required
                value={form.categoryId}
                onChange={(event) => handleCategoryChange(event.target.value)}
                disabled={categoriesLoading}
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
                disabled={subCategoriesLoading || !form.categoryId}
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

          <button type="submit" className={styles.submit} disabled={registerState.isLoading}>
            {registerState.isLoading ? "등록 중..." : "제품 등록"}
          </button>
        </form>
      </article>

      {showSuccessModal ? (
        <div className={styles.backdrop} onClick={() => setShowSuccessModal(false)}>
          <div className={styles.modal} onClick={(event) => event.stopPropagation()}>
            <div className={styles.modalIcon}>
              <CheckCircle2 size={28} />
            </div>
            <p className={styles.modalMessage}>제품을 등록했습니다.</p>
            <button
              type="button"
              className={styles.modalConfirm}
              onClick={() => setShowSuccessModal(false)}
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
