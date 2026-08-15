// 어드민 수리케이스 전체 조회 처리량(throughput) 부하테스트.
//
// 테스트 대상:
//   GET /api/repair-cases  — 어드민 전체 목록 (페이지네이션 없으면 전 건 조회)
//
// 기사 계정과 달리 ADMIN 권한이 필요하다.
// setup()에서 한 번 로그인해 세션 쿠키를 공유하고, VU는 조회만 반복한다.
//
// 실행 예시:
//   k6 run \
//     -e BASE_URL=https://noomit.abcganada.xyz \
//     -e TEST_EMAIL=<어드민 계정> \
//     -e TEST_PASSWORD=<비번> \
//     -e PEAK_VUS=5 \
//     k6/repair-load-test/repair-admin-throughput.ts

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
    "http_req_duration{name:admin-list}": ["p(95)<1000"],
    checks: ["rate>0.99"],
  },
};

interface CsrfResponse {
  headerName: string;
  token: string;
}

interface SetupData {
  sessionCookie: string;
}

export function setup(): SetupData {
  if (!EMAIL || !PASSWORD) {
    throw new Error("TEST_EMAIL / TEST_PASSWORD 환경변수가 필요합니다.");
  }

  // 1) CSRF 발급
  const csrfRes = http.get(`${BASE_URL}/auth/csrf`);
  if (csrfRes.status !== 200) throw new Error("CSRF 발급 실패");
  const csrf = csrfRes.json() as unknown as CsrfResponse;

  // 2) 어드민 계정으로 로그인
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
  if (!sessionCookie) throw new Error("세션 쿠키 추출 실패");

  return { sessionCookie };
}

export default function (data: SetupData): void {
  const { sessionCookie } = data;

  // 어드민 전체 수리케이스 조회
  const res = http.get(`${BASE_URL}/api/repair-cases`, {
    headers: { Cookie: sessionCookie },
    tags: { name: "admin-list" },
  });
  check(res, { "admin-list 200": (r) => r.status === 200 });

  sleep(1);
}
