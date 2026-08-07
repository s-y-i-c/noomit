"use client";

import { useState, type FormEvent } from "react";
import { Contact, RefreshCw, Search } from "lucide-react";
import { useGetCustomersQuery } from "../api/customersApi";
import type { CustomerFilters, CustomerStatus } from "../types/customer";
import { CustomerDetailModal } from "./CustomerDetailModal";
import styles from "./CustomerTable.module.css";

const PAGE_SIZE = 20;

function initialFilters(): CustomerFilters {
  return { keyword: "", status: "", page: 0, size: PAGE_SIZE, sort: "name" };
}

export function CustomerTable() {
  const [filters, setFilters] = useState<CustomerFilters>(initialFilters);
  const [keywordDraft, setKeywordDraft] = useState("");
  const [selectedCustomerId, setSelectedCustomerId] = useState<string | null>(null);
  const { data, isFetching, error } = useGetCustomersQuery(filters);

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

  const changeStatusFilter = (status: CustomerStatus | "") => {
    setFilters((current) => ({ ...current, status, page: 0 }));
  };

  const totalPages = data?.totalPages ?? 0;

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><Contact size={15} /> Customers</p>
          <h1>고객 관리</h1>
          <p>이름 또는 전화번호로 고객을 검색합니다. 비활성 고객도 함께 표시됩니다.</p>
        </div>
        {data ? <span className={styles.total}>총 {data.totalElements.toLocaleString()}명</span> : null}
      </header>

      <form className={styles.filters} onSubmit={submitSearch}>
        <label>
          <span>검색어</span>
          <input
            value={keywordDraft}
            onChange={(e) => setKeywordDraft(e.target.value)}
            placeholder="이름 또는 전화번호"
          />
        </label>
        <label>
          <span>상태</span>
          <select
            value={filters.status}
            onChange={(e) => changeStatusFilter(e.target.value as CustomerStatus | "")}
          >
            <option value="">전체</option>
            <option value="ACTIVE">활성</option>
            <option value="INACTIVE">비활성</option>
          </select>
        </label>
        <button className={styles.searchButton} type="submit" disabled={isFetching}>
          {isFetching ? <RefreshCw className={styles.spinning} size={17} /> : <Search size={17} />}
          {isFetching ? "조회 중" : "검색"}
        </button>
      </form>

      {errorMessage ? <div className={styles.error}>{errorMessage}</div> : null}

      <article className={styles.panel}>
        {data && data.customers.length ? (
          <>
            <div className={styles.tableWrap}>
              <table>
                <thead>
                  <tr>
                    <th>이름</th>
                    <th>전화번호</th>
                    <th>주소</th>
                    <th>상태</th>
                  </tr>
                </thead>
                <tbody>
                  {data.customers.map((customer) => (
                    <tr
                      key={customer.id}
                      className={styles.row}
                      onClick={() => setSelectedCustomerId(customer.id)}
                    >
                      <td>
                        <strong>{customer.name}</strong>
                      </td>
                      <td>{customer.phoneNumber}</td>
                      <td>
                        {customer.address}
                        {customer.detailAddress ? ` ${customer.detailAddress}` : ""}
                        <small>{customer.zipCode}</small>
                      </td>
                      <td>
                        <span className={styles.statusBadge} data-status={customer.status}>
                          {customer.status === "ACTIVE" ? "활성" : "비활성"}
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
            {isFetching ? "불러오는 중..." : "조회된 고객이 없습니다."}
          </div>
        )}
      </article>

      {selectedCustomerId ? (
        <CustomerDetailModal
          customerId={selectedCustomerId}
          onClose={() => setSelectedCustomerId(null)}
        />
      ) : null}
    </section>
  );
}
