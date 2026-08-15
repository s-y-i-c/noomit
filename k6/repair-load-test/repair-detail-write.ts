// 수리 내역 등록 쓰기 처리량(throughput) 부하테스트.
//
// 테스트 대상:
//   POST /api/repair-cases/{id}/details — 수리 항목 추가 (description + amount)
//
// 쓰기 경로의 DB 처리량과 HikariCP 커넥션 소비를 측정한다.
// setup()에서 기사 계정으로 1회 로그인, IN_PROGRESS 케이스를 자동 탐색.
// VU들은 같은 케이스에 수리 내역을 반복 추가하며 쓰기 처리량을 측정한다.
//
// 실행 예시:
//   k6 run \
//     -e BASE_URL=https://noomit.abcganada.xyz \
//     -e TEST_EMAIL=<기사 계정> \
//     -e TEST_PASSWORD=<비번> \
//     -e PEAK_VUS=20 \
//     k6/repair-load-test/repair-detail-write.ts

import http from "k6/http";
import { check, sleep } from "k6";
import type { Options } from "k6/options";
import { login } from "../lib/auth.ts";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(/\/+$/, "");
const EMAIL = __ENV.TEST_EMAIL || "";
const PASSWORD = __ENV.TEST_PASSWORD || "";
const PEAK_VUS = Number(__ENV.PEAK_VUS || 10);

export const options: Options = {
  stages: [
    { duration: "30s", target: PEAK_VUS },  // 램프업
    { duration: "1m",  target: PEAK_VUS },  // 유지(측정 구간)
    { duration: "30s", target: 0 },         // 램프다운
  ],
  thresholds: {
    http_req_failed: ["rate<0.01"],
    "http_req_duration{name:add-detail}": ["p(95)<1000"],
    checks: ["rate>0.99"],
  },
};

interface CsrfResponse {
  headerName: string;
  token: string;
}

interface RepairCase {
  id: string;
  status: string;
}

interface ApiResponse<T> {
  data?: T;
}

interface SetupData {
  sid: string;
  csrfHeader: string;
  csrfToken: string;
  caseId: string;
}

export function setup(): SetupData {
  if (!EMAIL || !PASSWORD) throw new Error("TEST_EMAIL / TEST_PASSWORD 환경변수가 필요합니다.");

  // 1) 기사 계정으로 로그인 (1회) — auth.ts 공용 헬퍼 사용
  const sid = login(BASE_URL, EMAIL, PASSWORD);
  if (!sid) throw new Error("로그인 실패 — 계정/비번 확인");

  // 2) IN_PROGRESS 케이스 자동 탐색
  const listRes = http.get(`${BASE_URL}/api/repair-cases/my`, {
    cookies: { JSESSIONID: sid },
  });
  if (listRes.status !== 200) throw new Error(`케이스 목록 조회 실패: ${listRes.status}`);

  const body = listRes.json() as unknown as ApiResponse<RepairCase[]>;
  const cases = body?.data ?? [];
  const inProgress = Array.isArray(cases)
    ? cases.find((c) => c.status === "IN_PROGRESS")
    : undefined;

  if (!inProgress) {
    throw new Error("IN_PROGRESS 상태인 RepairCase가 없습니다. 테스트 전에 담당 케이스를 생성해두세요.");
  }

  // 3) 쓰기용 CSRF 발급
  const csrfRes = http.get(`${BASE_URL}/auth/csrf`, {
    cookies: { JSESSIONID: sid },
  });
  if (csrfRes.status !== 200) throw new Error("CSRF 발급 실패");
  const csrf = csrfRes.json() as unknown as CsrfResponse;

  console.log(`[setup] 대상 케이스: ${inProgress.id} | PEAK_VUS: ${PEAK_VUS}`);
  return { sid, csrfHeader: csrf.headerName, csrfToken: csrf.token, caseId: inProgress.id };
}

const DESCRIPTIONS = [
  "부품 교체",
  "모터 수리",
  "배선 점검 및 교체",
  "냉각 시스템 청소",
  "소프트웨어 업데이트",
  "센서 보정",
  "오일 교환",
  "필터 교체",
];

export default function (data: SetupData): void {
  const { sid, csrfHeader, csrfToken, caseId } = data;

  const description = DESCRIPTIONS[Math.floor(Math.random() * DESCRIPTIONS.length)];
  const amount = Math.floor(Math.random() * 90_000) + 10_000; // 10,000 ~ 100,000

  const res = http.post(
    `${BASE_URL}/api/repair-cases/${caseId}/details`,
    JSON.stringify({ description, amount }),
    {
      cookies: { JSESSIONID: sid },
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken,
      },
      tags: { name: "add-detail" },
    },
  );

  check(res, { "수리 내역 추가 200": (r) => r.status === 200 });
  sleep(1);
}
