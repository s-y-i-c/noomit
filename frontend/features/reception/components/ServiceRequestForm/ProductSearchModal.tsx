"use client";

import { useEffect, useState, type KeyboardEvent } from "react";
import { Search, X } from "lucide-react";
import {
  useGetCategoriesQuery,
  useGetSubCategoriesQuery,
  useLazySearchProductsQuery,
} from "@/features/products/api/productsApi";
import type { Product } from "@/features/products/types/product";
import styles from "./ProductSearchModal.module.css";

interface ProductSearchModalProps {
  /** 검색 버튼을 누르기 전 모델코드 입력칸에 있던 값 — 모달을 열자마자 그걸로 바로 검색한다. */
  initialKeyword: string;
  onSelect: (product: Product) => void;
  onClose: () => void;
}

export function ProductSearchModal({ initialKeyword, onSelect, onClose }: ProductSearchModalProps) {
  const [keyword, setKeyword] = useState(initialKeyword);
  const [hasSearched, setHasSearched] = useState(() => Boolean(initialKeyword.trim()));

  const { data: categories } = useGetCategoriesQuery();
  const { data: subCategories } = useGetSubCategoriesQuery();
  const [searchProducts, { data: results, isFetching, isError }] = useLazySearchProductsQuery();

  const categoryName = (categoryId: number) =>
    categories?.find((category) => category.id === String(categoryId))?.name ?? "-";
  const subCategoryName = (subCategoryId: number) =>
    subCategories?.find((sub) => sub.id === String(subCategoryId))?.name ?? "-";

  const runSearch = (value: string) => {
    const trimmed = value.trim();
    if (!trimmed) return;
    setHasSearched(true);
    void searchProducts(trimmed);
  };

  // 모달을 열자마자, 모델코드 칸에 이미 입력돼 있던 값으로 바로 한 번 검색해준다.
  // hasSearched는 이미 initialKeyword 기준으로 초기화돼 있으니 여기서는 조회만 트리거한다.
  useEffect(() => {
    const trimmed = initialKeyword.trim();
    if (trimmed) void searchProducts(trimmed);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key !== "Enter") return;
    event.preventDefault();
    runSearch(keyword);
  };

  return (
    <div className={styles.backdrop} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2>제품 검색</h2>
          <button type="button" onClick={onClose} aria-label="닫기" className={styles.closeButton}>
            <X size={18} />
          </button>
        </div>

        <div className={styles.searchRow}>
          <input
            autoFocus
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="모델코드 일부만 입력해도 검색됩니다 (예: 2024)"
          />
          <button type="button" onClick={() => runSearch(keyword)} disabled={isFetching || !keyword.trim()}>
            <Search size={15} /> {isFetching ? "검색 중..." : "검색"}
          </button>
        </div>

        {isError ? <div className={styles.state}>검색 중 문제가 발생했습니다. 다시 시도해주세요.</div> : null}

        {hasSearched && !isFetching && results && results.length === 0 ? (
          <div className={styles.state}>일치하는 제품이 없습니다. 창을 닫고 직접 입력해주세요.</div>
        ) : null}

        {results && results.length ? (
          <ul className={styles.resultList}>
            {results.map((product) => (
              <li key={product.id}>
                <button type="button" onClick={() => onSelect(product)}>
                  <strong>{product.modelCode}</strong>
                  <span>{product.modelName}</span>
                  <small>{categoryName(product.categoryId)} · {subCategoryName(product.subCategoryId)}</small>
                </button>
              </li>
            ))}
          </ul>
        ) : null}
      </div>
    </div>
  );
}
