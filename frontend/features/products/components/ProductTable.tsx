"use client";

import { useMemo, useState, type FormEvent } from "react";
import Link from "next/link";
import { Package, Plus, RefreshCw, RotateCcw, Search } from "lucide-react";
import { useGetCategoriesQuery, useGetProductsQuery, useGetSubCategoriesQuery } from "../api/productsApi";
import type { ProductFilters, ProductStatus } from "../types/product";
import { ProductDetailModal } from "./ProductDetailModal";
import styles from "./ProductTable.module.css";

const PAGE_SIZE = 20;

function initialFilters(): ProductFilters {
  return { keyword: "", categoryId: "", subCategoryId: "", status: "", page: 0, size: PAGE_SIZE, sort: "modelName" };
}

interface ProductTableProps {
  /** true면 등록 링크를 숨기고, 상세 모달도 조회 전용으로 연다 (기사 등 조회 전용 화면용). */
  readOnly?: boolean;
}

export function ProductTable({ readOnly = false }: ProductTableProps) {
  const [filters, setFilters] = useState<ProductFilters>(initialFilters);
  const [keywordDraft, setKeywordDraft] = useState("");
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null);

  const { data: categories } = useGetCategoriesQuery();
  const { data: subCategories } = useGetSubCategoriesQuery();
  const { data, isFetching, error } = useGetProductsQuery(filters);

  const availableSubCategories = useMemo(
    () => (subCategories ?? []).filter((sub) => sub.categoryId === filters.categoryId),
    [subCategories, filters.categoryId],
  );

  const categoryName = (categoryId: number) =>
    categories?.find((category) => category.id === String(categoryId))?.name ?? "-";
  const subCategoryName = (subCategoryId: number) =>
    subCategories?.find((sub) => sub.id === String(subCategoryId))?.name ?? "-";

  const errorMessage = typeof error === "object" && error !== null && "message" in error
    ? String(error.message)
    : null;

  const submitSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFilters((current) => ({ ...current, keyword: keywordDraft.trim(), page: 0 }));
  };

  const goToPage = (page: number) => {
    setFilters((current) => ({ ...current, page }));
  };

  const changeCategoryFilter = (categoryId: string) => {
    setFilters((current) => ({ ...current, categoryId, subCategoryId: "", page: 0 }));
  };

  const changeSubCategoryFilter = (subCategoryId: string) => {
    setFilters((current) => ({ ...current, subCategoryId, page: 0 }));
  };

  const changeStatusFilter = (status: ProductStatus | "") => {
    setFilters((current) => ({ ...current, status, page: 0 }));
  };

  const resetFilters = () => {
    setKeywordDraft("");
    setFilters(initialFilters());
  };

  const totalPages = data?.totalPages ?? 0;

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><Package size={15} /> Products</p>
          <h1>제품 목록</h1>
          <p>모델명 또는 모델코드로 제품을 검색합니다. 단종 제품도 함께 표시됩니다.</p>
        </div>
        <div className={styles.heroActions}>
          {data ? <span className={styles.total}>총 {data.totalElements.toLocaleString()}개</span> : null}
          {readOnly ? null : (
            <Link href="/admin/products/register" className={styles.registerLink}>
              <Plus size={15} /> 제품 등록
            </Link>
          )}
        </div>
      </header>

      <form className={styles.filters} onSubmit={submitSearch}>
        <label>
          <span>검색어</span>
          <input
            value={keywordDraft}
            onChange={(e) => setKeywordDraft(e.target.value)}
            placeholder="모델명 또는 모델코드"
          />
        </label>
        <label>
          <span>카테고리</span>
          <select value={filters.categoryId} onChange={(e) => changeCategoryFilter(e.target.value)}>
            <option value="">전체</option>
            {(categories ?? []).map((category) => (
              <option key={category.id} value={category.id}>{category.name}</option>
            ))}
          </select>
        </label>
        <label>
          <span>서브카테고리</span>
          <select
            value={filters.subCategoryId}
            onChange={(e) => changeSubCategoryFilter(e.target.value)}
            disabled={!filters.categoryId}
          >
            <option value="">전체</option>
            {availableSubCategories.map((sub) => (
              <option key={sub.id} value={sub.id}>{sub.name}</option>
            ))}
          </select>
        </label>
        <label>
          <span>상태</span>
          <select
            value={filters.status}
            onChange={(e) => changeStatusFilter(e.target.value as ProductStatus | "")}
          >
            <option value="">전체</option>
            <option value="ACTIVE">판매중</option>
            <option value="INACTIVE">단종</option>
          </select>
        </label>
        <button className={styles.searchButton} type="submit" disabled={isFetching}>
          {isFetching ? <RefreshCw className={styles.spinning} size={17} /> : <Search size={17} />}
          {isFetching ? "조회 중" : "검색"}
        </button>
        <button className={styles.resetButton} type="button" onClick={resetFilters} disabled={isFetching}>
          <RotateCcw size={17} /> 초기화
        </button>
      </form>

      {errorMessage ? <div className={styles.error}>{errorMessage}</div> : null}

      <article className={styles.panel}>
        {data && data.products.length ? (
          <>
            <div className={styles.tableWrap}>
              <table>
                <thead>
                  <tr>
                    <th>모델명</th>
                    <th>모델코드</th>
                    <th>카테고리</th>
                    <th>서브카테고리</th>
                    <th>상태</th>
                  </tr>
                </thead>
                <tbody>
                  {data.products.map((product) => (
                    <tr
                      key={product.id}
                      className={styles.row}
                      onClick={() => setSelectedProductId(product.id)}
                    >
                      <td><strong>{product.modelName}</strong></td>
                      <td>{product.modelCode}</td>
                      <td>{categoryName(product.categoryId)}</td>
                      <td>{subCategoryName(product.subCategoryId)}</td>
                      <td>
                        <span className={styles.statusBadge} data-status={product.status}>
                          {product.status === "ACTIVE" ? "판매중" : "단종"}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className={styles.pagination}>
              <button
                type="button"
                onClick={() => goToPage(filters.page - 1)}
                disabled={filters.page <= 0 || isFetching}
              >
                이전
              </button>
              <span>{filters.page + 1} / {Math.max(totalPages, 1)} 페이지</span>
              <button
                type="button"
                onClick={() => goToPage(filters.page + 1)}
                disabled={filters.page + 1 >= totalPages || isFetching}
              >
                다음
              </button>
            </div>
          </>
        ) : (
          <div className={styles.emptyRows}>
            {isFetching ? "불러오는 중..." : "조회된 제품이 없습니다."}
          </div>
        )}
      </article>

      {selectedProductId ? (
        <ProductDetailModal
          productId={selectedProductId}
          onClose={() => setSelectedProductId(null)}
          readOnly={readOnly}
        />
      ) : null}
    </section>
  );
}
