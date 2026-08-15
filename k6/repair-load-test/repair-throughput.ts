// 수리 케이스 조회 처리량(throughput) 부하테스트.
//
// 수리 도메인의 주요 읽기 경로를 검증한다:
//   목록 조회 → 케이스 상세 조회 (로그인은 setup()에서 한 번만)
//
// 로그인을 매 iteration마다 하면 BCrypt CPU 비용이 수리 조회 병목을 가린다.
// setup()에서 한 번 로그인해 세션 쿠키를 공유하고, VU는 조회만 반복한다.
// VU를 올리며 "어느 처리량에서 p95가 튀고 HikariCP pending이 생기는지"를
// 찾는 것이 목적이다. 서버 지표는 monitoring/ 의 Grafana 로 본다.
//
// 실행 예시:
//   k6 run \
//     -e BASE_URL=https://noomit.abcganada.xyz \
//     -e TEST_EMAIL=<기사 계정> \
//     -e TEST_PASSWORD=<비번> \
//     -e PEAK_VUS=5 \
//     k6/repair-load-test/repair-throughput.ts

import http from "k6/http";
import { check, sleep } from "k6";
import type { Options } from "k6/options";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(/\/+$/, "");
const EMAIL = __ENV.TEST_EMAIL || "";
const PASSWORD = __ENV.TEST_PASSWORD || "";
const PEAK_VUS = Number(__ENV.PEAK_VUS || 5);

export const options: Options = {
  stages: [
    { duration: "30s", target: PEAK_VUS }, // 램프업
    { duration: "1m", target: PEAK_VUS },  // 유지(측정 구간)
    { duration: "30s", target: 0 },        // 램프다운
  ],
  thresholds: {
    http_req_failed: ["rate<0.01"],
    "http_req_duration{name:list}": ["p(95)<1000"],
    "http_req_duration{name:detail}": ["p(95)<1000"],
    checks: ["rate>0.99"],
  },
};

interface CsrfResponse {
  headerName: string;
  token: string;
}

interface RepairCase {
  id: string;
}

interface ApiResponse<T> {
  success: boolean;
  data?: T;
}

interface SetupData {
  sessionCookie: string;
  firstCaseId: string | null;
}

export function setup(): SetupData {
  if (!EMAIL || !PASSWORD) {
    throw new Error("TEST_EMAIL / TEST_PASSWORD 환경변수가 필요합니다.");
  }

  // 1) CSRF 발급
  const csrfRes = http.get(`${BASE_URL}/auth/csrf`);
  if (csrfRes.status !== 200) throw new Error("CSRF 발급 실패");
  const csrf = csrfRes.json() as unknown as CsrfResponse;

  // 2) 로그인 — 세션 쿠키 추출
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email: EMAIL, password: PASSWORD }),
    {
      headers: {
        "Content-Type": "application/json",
        [csrf.headerName]: csrf.token,
      },
    },
  );
  if (loginRes.status !== 200) throw new Error(`로그인 실패: ${loginRes.status}`);

  const setCookie = loginRes.headers["Set-Cookie"] ?? "";
  const sessionMatch = setCookie.match(/JSESSIONID=[^;]+/);
  const sessionCookie = sessionMatch ? sessionMatch[0] : "";

  // 3) 첫 번째 케이스 ID 미리 조회 (상세 조회용)
  const listRes = http.get(`${BASE_URL}/api/repair-cases/my`, {
    headers: { Cookie: sessionCookie },
  });
  let firstCaseId: string | null = null;
  try {
    const body = listRes.json() as unknown as ApiResponse<RepairCase[]>;
    const cases = body?.data;
    if (Array.isArray(cases) && cases.length > 0) {
      firstCaseId = cases[0].id;
    }
  } catch { /* 무시 */ }

  return { sessionCookie, firstCaseId };
}

export default function (data: SetupData): void {
  const { sessionCookie, firstCaseId } = data;
  const headers = { Cookie: sessionCookie };

  // 목록 조회
  const listRes = http.get(`${BASE_URL}/api/repair-cases/my`, {
    headers,
    tags: { name: "list" },
  });
  check(listRes, { "목록 200": (r) => r.status === 200 });

  // 상세 조회
  if (firstCaseId) {
    const detailRes = http.get(`${BASE_URL}/api/repair-cases/${firstCaseId}`, {
      headers,
      tags: { name: "detail" },
    });
    check(detailRes, { "상세 200": (r) => r.status === 200 });
  }

  sleep(1);
}
