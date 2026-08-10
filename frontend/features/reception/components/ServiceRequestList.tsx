"use client";

import { useState } from "react";
import Link from "next/link";
import { Inbox, Plus, RefreshCw } from "lucide-react";
import { useGetServiceRequestsQuery } from "../api/serviceRequestApi";
import type { ServiceRequestFilters, ServiceRequestStatus } from "../types/serviceRequest";
import { Dropdown } from "./Dropdown";
import { Pagination } from "./Pagination";
import { ServiceRequestTable } from "./ServiceRequestList/ServiceRequestTable";
import styles from "./ServiceRequestList.module.css";

const PAGE_SIZE = 15;

const STATUS_OPTIONS: { value: ServiceRequestStatus | ""; label: string }[] = [
  { value: "", label: "전체" },
  { value: "RECEIVED", label: "접수중" },
  { value: "ASSIGNED", label: "배정됨" },
  { value: "CANCELLED", label: "취소" },
];

const SORT_OPTIONS: { value: ServiceRequestFilters["sort"]; label: string }[] = [
  { value: "requestedAt,desc", label: "최신순" },
  { value: "requestedAt,asc", label: "오래된순" },
];

function initialFilters(): ServiceRequestFilters {
  return { status: "", sort: "requestedAt,desc", page: 0, size: PAGE_SIZE };
}

export function ServiceRequestList() {
  const [filters, setFilters] = useState<ServiceRequestFilters>(initialFilters);
  const { data, isFetching, error } = useGetServiceRequestsQuery(filters);

  const errorMessage = typeof error === "object" && error !== null && "message" in error
    ? String(error.message)
    : null;

  const changeStatusFilter = (status: ServiceRequestStatus | "") => {
    setFilters((current) => ({ ...current, status, page: 0 }));
  };

  const changeSort = (sort: ServiceRequestFilters["sort"]) => {
    setFilters((current) => ({ ...current, sort, page: 0 }));
  };

  const goToPage = (page: number) => {
    setFilters((current) => ({ ...current, page }));
  };

  const totalPages = data ? Math.ceil(data.totalElements / data.size) : 0;

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><Inbox size={15} /> Reception</p>
          <h1>접수 관리</h1>
          <p>고객 A/S 접수 내역을 확인합니다.</p>
        </div>
        {data ? <span className={styles.total}>총 {data.totalElements.toLocaleString()}건</span> : null}
      </header>

      <div className={styles.filters}>
        <Dropdown label="상태" value={filters.status} options={STATUS_OPTIONS} onChange={changeStatusFilter} />
        <Dropdown label="정렬" value={filters.sort} options={SORT_OPTIONS} onChange={changeSort} />
        {isFetching ? <RefreshCw className={styles.spinning} size={17} /> : null}
        <Link href="/counselor/reception/new" className={styles.createButton}>
          <Plus size={15} /> 접수 생성
        </Link>
      </div>

      {errorMessage ? <div className={styles.error}>{errorMessage}</div> : null}

      <article className={styles.panel}>
        {data && data.content.length ? (
          <>
            <ServiceRequestTable items={data.content} />
            <Pagination
              page={filters.page}
              totalPages={totalPages}
              isFetching={isFetching}
              onChange={goToPage}
            />
          </>
        ) : (
          <div className={styles.emptyRows}>
            {isFetching ? "불러오는 중..." : "접수 내역이 없습니다."}
          </div>
        )}
      </article>
    </section>
  );
}