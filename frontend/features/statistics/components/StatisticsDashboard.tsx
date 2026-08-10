"use client";

import { useMemo, useState } from "react";
import { BarChart3, CalendarDays, CheckCircle2, CircleDollarSign, RefreshCw, Search, Wrench, XCircle } from "lucide-react";
import { useLazyGetStatisticsDashboardQuery } from "../api/statisticsApi";
import type { StatisticsDashboardData, StatisticsFilters } from "../types/statistics";
import styles from "./StatisticsDashboard.module.css";

function dateInputValue(date: Date): string {
  const offset = date.getTimezoneOffset();
  return new Date(date.getTime() - offset * 60_000).toISOString().slice(0, 10);
}

function initialFilters(): StatisticsFilters {
  const to = new Date();
  const from = new Date(to);
  from.setDate(from.getDate() - 29);
  return {
    from: dateInputValue(from),
    to: dateInputValue(to),
    technicianId: "",
    customerId: "",
    productId: "",
    status: "",
    repeatWindowDays: 30,
  };
}

function won(value: number): string {
  return `${value.toLocaleString()}원`;
}

function EmptyRows({ label }: { label: string }) {
  return <div className={styles.emptyRows}>{label} 데이터가 없습니다.</div>;
}

export function StatisticsDashboard() {
  const [draft, setDraft] = useState<StatisticsFilters>(initialFilters);
  const [load, { data, isFetching, error }] = useLazyGetStatisticsDashboardQuery();

  const maxTrend = useMemo(() => Math.max(1, ...(data?.trends.map((item) => item.receivedCount) ?? [1])), [data]);
  const errorMessage = typeof error === "object" && error !== null && "message" in error
    ? String(error.message)
    : null;

  const update = <K extends keyof StatisticsFilters>(key: K, value: StatisticsFilters[K]) => {
    setDraft((current) => ({ ...current, [key]: value }));
  };

  const submit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    void load(draft);
  };

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}><BarChart3 size={15} /> Statistics</p>
          <h1>기간별 A/S 통계</h1>
          <p>필요한 조건을 선택하고 조회 버튼을 눌러 현재 데이터를 집계합니다.</p>
        </div>
        {data ? <span className={styles.period}><CalendarDays size={16} />{data.period.from} — {data.period.to}</span> : null}
      </header>

      <form className={styles.filters} onSubmit={submit}>
        <label><span>시작일</span><input type="date" value={draft.from} onChange={(e) => update("from", e.target.value)} required /></label>
        <label><span>종료일</span><input type="date" value={draft.to} onChange={(e) => update("to", e.target.value)} required /></label>
        <label><span>담당 기사</span><input value={draft.technicianId} onChange={(e) => update("technicianId", e.target.value)} placeholder="기사 ID" /></label>
        <label><span>고객</span><input value={draft.customerId} onChange={(e) => update("customerId", e.target.value)} placeholder="고객 ID" /></label>
        <label><span>제품</span><input value={draft.productId} onChange={(e) => update("productId", e.target.value)} placeholder="제품 ID" /></label>
        <label><span>상태</span><select value={draft.status} onChange={(e) => update("status", e.target.value as StatisticsFilters["status"])}><option value="">전체</option><option value="RECEIVED">접수</option><option value="ASSIGNED">배정</option><option value="IN_PROGRESS">수리 중</option><option value="COMPLETED">완료</option><option value="CANCELLED">취소</option></select></label>
        <label><span>재수리 기준</span><select value={draft.repeatWindowDays} onChange={(e) => update("repeatWindowDays", Number(e.target.value))}><option value={7}>7일</option><option value={30}>30일</option><option value={90}>90일</option></select></label>
        <button className={styles.searchButton} type="submit" disabled={isFetching}>{isFetching ? <RefreshCw className={styles.spinning} size={17} /> : <Search size={17} />}{isFetching ? "집계 중" : "통계 조회"}</button>
      </form>

      {errorMessage ? <div className={styles.error}>{errorMessage}</div> : null}
      {data && !data.integration.connected ? <div className={styles.integration}><Wrench size={18} /><div><strong>연동 준비 상태</strong><p>{data.integration.message} 현재 화면과 계산 구조는 정상 동작하며, 조회 어댑터 연결 후 실제 수치가 표시됩니다.</p></div></div> : null}

      {!data && !errorMessage ? <div className={styles.initialState}><Search size={24} /><strong>조회 조건을 확인해 주세요.</strong><p>통계 조회 버튼을 누르면 선택한 기간의 데이터를 그때 집계합니다.</p></div> : null}

      {data?.integration.connected ? <DashboardContent data={data} maxTrend={maxTrend} /> : null}
    </section>
  );
}

function DashboardContent({ data, maxTrend }: { data: StatisticsDashboardData; maxTrend: number }) {
  const cards = [
    { label: "총 접수", value: data.summary.receivedCount.toLocaleString(), unit: "건", icon: <BarChart3 /> },
    { label: "수리 완료", value: data.summary.completedCount.toLocaleString(), unit: "건", icon: <CheckCircle2 /> },
    { label: "진행 중", value: data.summary.inProgressCount.toLocaleString(), unit: "건", icon: <Wrench /> },
    { label: "취소", value: data.summary.cancelledCount.toLocaleString(), unit: "건", icon: <XCircle /> },
    { label: "완료율", value: data.summary.completionRate.toFixed(1), unit: "%", icon: <CheckCircle2 /> },
    { label: "수리 금액 합계", value: won(data.summary.totalRepairAmount), unit: "", icon: <CircleDollarSign /> },
  ];
  return <>
    <div className={styles.summaryGrid}>{cards.map((card) => <article className={styles.summaryCard} key={card.label}><div className={styles.cardTop}><span>{card.label}</span><i>{card.icon}</i></div><strong>{card.value}<small>{card.unit}</small></strong></article>)}</div>

    <div className={styles.twoColumns}>
      <article className={styles.panel}>
        <div className={styles.panelHeader}><div><p className={styles.kicker}>FLOW</p><h2>접수 추이</h2></div><div className={styles.legend}><span data-tone="received" />접수</div></div>
        {data.trends.length ? <div className={styles.chart}>{data.trends.map((point, index) => <div className={styles.chartColumn} key={point.date} title={`${point.date} · 접수 ${point.receivedCount}`}><div className={styles.bars}><i data-tone="received" style={{ height: `${Math.max(2, point.receivedCount / maxTrend * 100)}%` }} /></div>{(index === 0 || index === data.trends.length - 1 || index % Math.max(1, Math.floor(data.trends.length / 5)) === 0) ? <span>{point.date.slice(5)}</span> : <span />}</div>)}</div> : <EmptyRows label="추이" />}
      </article>
      <article className={styles.panel}>
        <div className={styles.panelHeader}><div><p className={styles.kicker}>REPEAT</p><h2>{data.repeatRepair.windowDays}일 이내 재접수율</h2></div></div>
        <div className={styles.repeatList}>{[
          ["동일 고객", data.repeatRepair.sameCustomerRate],
          ["동일 고객 제품", data.repeatRepair.sameProductRate],
          ["동일 기사·제품", data.repeatRepair.sameTechnicianSameProductRate],
        ].map(([label, rate]) => <div className={styles.repeatItem} key={String(label)}><div><span>{label}</span><strong>{Number(rate).toFixed(1)}%</strong></div><div className={styles.track}><i style={{ width: `${Math.min(100, Number(rate))}%` }} /></div></div>)}</div>
      </article>
    </div>

    <article className={styles.panel}>
      <div className={styles.panelHeader}><div><p className={styles.kicker}>PERFORMANCE</p><h2>기사별 처리 현황</h2></div></div>
      {data.technicians.length ? <div className={styles.tableWrap}><table><thead><tr><th>담당 기사</th><th>배정</th><th>완료</th><th>완료율</th><th>수리 금액</th></tr></thead><tbody>{data.technicians.map((row) => <tr key={row.technicianId}><td><strong>{row.technicianName}</strong><small>{row.technicianId}</small></td><td>{row.assignedCount}건</td><td>{row.completedCount}건</td><td><span className={styles.rateBadge}>{row.completionRate.toFixed(1)}%</span></td><td>{won(row.totalRepairAmount)}</td></tr>)}</tbody></table></div> : <EmptyRows label="기사별" />}
    </article>

    <div className={styles.twoColumns}>
      <Ranking title="고객별 접수" rows={data.customers.map((row) => ({ id: row.customerId, name: row.customerName, count: row.requestCount, rate: row.repeatRate }))} />
      <Ranking title="제품별 접수" rows={data.products.map((row) => ({ id: row.productId, name: row.productName, count: row.requestCount, rate: row.repeatRate }))} />
    </div>
  </>;
}

function Ranking({ title, rows }: { title: string; rows: Array<{ id: string; name: string; count: number; rate: number }> }) {
  return <article className={styles.panel}><div className={styles.panelHeader}><div><p className={styles.kicker}>RANKING</p><h2>{title}</h2></div></div>{rows.length ? <div className={styles.ranking}>{rows.map((row, index) => <div className={styles.rankRow} key={row.id}><b>{String(index + 1).padStart(2, "0")}</b><div><strong>{row.name}</strong><small>{row.id}</small></div><span>{row.count}건</span><em>재접수 {row.rate.toFixed(1)}%</em></div>)}</div> : <EmptyRows label={title} />}</article>;
}
