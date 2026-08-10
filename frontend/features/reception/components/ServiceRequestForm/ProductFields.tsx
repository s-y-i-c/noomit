"use client";

import { useMemo, useState } from "react";
import { Search, X } from "lucide-react";
import { useGetCategoriesQuery, useGetSubCategoriesQuery } from "@/features/products/api/productsApi";
import type { Product } from "@/features/products/types/product";
import { ProductSearchModal } from "./ProductSearchModal";
import { emptyProductFieldsValue, type ProductFieldsValue } from "./productFieldsUtils";
import styles from "./ProductFields.module.css";

export type { ProductFieldsValue };

interface ProductFieldsProps {
  initialValue?: ProductFieldsValue; // 접수 수정 화면에 기존값 표시 위함
  onChange: (value: ProductFieldsValue) => void;
}

export function ProductFields({ initialValue, onChange }: ProductFieldsProps) {
  const [value, setValue] = useState<ProductFieldsValue>(initialValue ?? emptyProductFieldsValue);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  const { data: categories, isLoading: categoriesLoading } = useGetCategoriesQuery();
  const { data: subCategories, isLoading: subCategoriesLoading } = useGetSubCategoriesQuery();

  const availableSubCategories = useMemo(
    () => (subCategories ?? []).filter((sub) => sub.categoryId === value.categoryId),
    [subCategories, value.categoryId],
  );

  const update = (patch: Partial<ProductFieldsValue>) => {
    setValue((current) => {
      const next = { ...current, ...patch };
      onChange(next);
      return next;
    });
  };

  // 카테고리/서브카테고리/모델명을 직접 고치면 화면에 있는 제품이 더 이상 검색으로 찾은 그 제품과
  // 같다고 보장할 수 없으므로, 매칭과 모델코드를 함께 해제한다 (검증 안 된 코드가 남아있으면 안 됨).
  const updateAndUnmatch = (patch: Partial<ProductFieldsValue>) => {
    update({ ...patch, modelCode: "", matchedProductId: null });
  };

  const clearModelCode = () => {
    update({ modelCode: "", matchedProductId: null });
  };

  const handleSelect = (product: Product) => {
    update({
      categoryId: String(product.categoryId),
      subCategoryId: String(product.subCategoryId),
      modelName: product.modelName,
      modelCode: product.modelCode,
      matchedProductId: product.id,
    });
    setIsSearchOpen(false);
  };

  return (
    <div className={styles.grid}>
      <label className={styles.field}>
        <span>카테고리</span>
        <select
          value={value.categoryId}
          onChange={(e) => updateAndUnmatch({ categoryId: e.target.value, subCategoryId: "" })}
          disabled={categoriesLoading}
        >
          <option value="">선택 안 함</option>
          {(categories ?? []).map((category) => (
            <option key={category.id} value={category.id}>{category.name}</option>
          ))}
        </select>
      </label>

      <label className={styles.field}>
        <span>서브카테고리</span>
        <select
          value={value.subCategoryId}
          onChange={(e) => updateAndUnmatch({ subCategoryId: e.target.value })}
          disabled={subCategoriesLoading || !value.categoryId}
        >
          <option value="">선택 안 함</option>
          {availableSubCategories.map((sub) => (
            <option key={sub.id} value={sub.id}>{sub.name}</option>
          ))}
        </select>
      </label>

      <label className={styles.field}>
        <span>모델명</span>
        <input
          value={value.modelName}
          onChange={(e) => updateAndUnmatch({ modelName: e.target.value })}
          placeholder="예: 김치냉장고 프리미엄"
        />
      </label>

      <label className={styles.field}>
        <span>모델코드 (검색으로만 채워짐)</span>
        <div className={styles.searchRow}>
          <input
            className={styles.readOnlyInput}
            value={value.modelCode}
            readOnly
            placeholder="검색으로 찾은 경우에만 표시됩니다"
          />
          {value.modelCode ? (
            <button type="button" className={styles.clearButton} onClick={clearModelCode} aria-label="모델코드 지우기">
              <X size={16} />
            </button>
          ) : null}
          <button
            type="button"
            className={styles.searchButton}
            onClick={() => setIsSearchOpen(true)}
            aria-label="모델코드로 제품 검색"
          >
            <Search size={16} />
          </button>
        </div>
      </label>

      {value.matchedProductId ? (
        <p className={`${styles.notice} ${styles.noticeSuccess}`}>검색으로 찾은 제품이 반영되어 있습니다.</p>
      ) : null}

      {isSearchOpen ? (
        <ProductSearchModal
          initialKeyword={value.modelCode}
          onSelect={handleSelect}
          onClose={() => setIsSearchOpen(false)}
        />
      ) : null}
    </div>
  );
}
