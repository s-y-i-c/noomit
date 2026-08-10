"use client";

import { useState } from "react";
import { CalendarCheck } from "lucide-react";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { useGetMyAssignedRequestsQuery } from "../api/assignedRequestApi";
import { AssignedRequestCard } from "./AssignedRequestCard";
import { DateNavigator } from "./DateNavigator";
import { parseDateParam, toDateParam, weekdayFull } from "./dateUtils";
import styles from "./MyAssignedRequestList.module.css";

function today(): string {
  return toDateParam(new Date());
}

export function MyAssignedRequestList() {
  const [selectedDate, setSelectedDate] = useState(today);
  const { data: requests, isFetching, error } = useGetMyAssignedRequestsQuery(selectedDate);

  const errorMessage = error ? queryErrorMessage(error, "배정 목록을 불러오지 못했습니다.") : null;
  const count = requests?.length ?? 0;

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><CalendarCheck size={15} /> Engineer</p>
          <h1>담당 접수</h1>
          <p>{weekdayFull(parseDateParam(selectedDate))} 기준 {count}건</p>
        </div>
      </header>

      <DateNavigator selectedDate={selectedDate} onSelect={setSelectedDate} />

      {errorMessage ? <div className={styles.error}>{errorMessage}</div> : null}

      <div className={styles.list}>
        {isFetching ? (
          Array.from({ length: 3 }, (_, i) => <div key={i} className={styles.skeletonCard} />)
        ) : requests && requests.length > 0 ? (
          requests.map((request) => <AssignedRequestCard key={request.serviceRequestId} request={request} />)
        ) : (
          <div className={styles.empty}>배정된 건이 없습니다.</div>
        )}
      </div>
    </section>
  );
}