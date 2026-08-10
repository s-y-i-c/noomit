"use client";

import { useState } from "react";
import { Inbox, RefreshCw } from "lucide-react";
import { useGetServiceRequestsQuery } from "../api/serviceRequestApi";
import type { ServiceRequestFilters, ServiceRequestStatus } from "../types/serviceRequest";
import { Pagination } from "./Pagination";
import { ServiceRequestTable } from "./ServiceRequestTable";
import styles from "./ServiceRequestList.module.css";

const PAGE_SIZE = 20;

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
        <label>
          <span>상태</span>
          <select
            value={filters.status}
            onChange={(e) => changeStatusFilter(e.target.value as ServiceRequestStatus | "")}
          >
            <option value="">전체</option>
            <option value="RECEIVED">접수중</option>
            <option value="ASSIGNED">배정됨</option>
            <option value="CANCELLED">취소</option>
          </select>
        </label>
        <label>
          <span>정렬</span>
          <select
            value={filters.sort}
            onChange={(e) => changeSort(e.target.value as ServiceRequestFilters["sort"])}
          >
            <option value="requestedAt,desc">최신순</option>
            <option value="requestedAt,asc">오래된순</option>
          </select>
        </label>
        {isFetching ? <RefreshCw className={styles.spinning} size={17} /> : null}
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