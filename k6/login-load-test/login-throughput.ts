// 로그인 처리량(throughput) 부하테스트.
//
// 로그인은 BCrypt 비밀번호 검증(의도적으로 CPU를 많이 쓰는 연산)을 거치므로,
// 1GB/-Xmx256m 운영 서버에서 CPU가 가장 먼저 포화되는 지점일 가능성이 높다.
// VU(가상 사용자)를 올리며 "어느 처리량에서 로그인 p95가 튀고 CPU가 100%에 붙는지"를
// 찾는 것이 목적이다. 서버 쪽 지표(CPU/p95/HikariCP/GC)는 monitoring/ 의 Grafana 로 본다.
//
// k6 는 TypeScript 를 네이티브로 실행한다(별도 빌드 불필요).
//   k6 run \
//    -e BASE_URL=...
//    -e TEST_EMAIL=...
//    -e TEST_PASSWORD=...
//    login-throughput.ts

import http from "k6/http";
import { check, sleep } from "k6";
import type { Options } from "k6/options";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(
  /\/+$/,
  "",
);
const EMAIL = __ENV.TEST_EMAIL || "";
const PASSWORD = __ENV.TEST_PASSWORD || "";
// 한계점을 찾을 땐 이 값을 올려가며 반복 실행한다 (예: -e PEAK_VUS=30).
const PEAK_VUS = Number(__ENV.PEAK_VUS || 10);

export const options: Options = {
  // 낮게 시작해 천천히 올린다 — 1GB 서버를 한 번에 죽이지 않기 위해.
  stages: [
    { duration: "30s", target: PEAK_VUS }, // 램프업
    { duration: "1m", target: PEAK_VUS }, // 유지(측정 구간)
    { duration: "30s", target: 0 }, // 램프다운
  ],
  thresholds: {
    // 전체 요청 실패율 1% 미만
    http_req_failed: ["rate<0.01"],
    // 로그인 요청만 따로 본 p95 (csrf GET 은 제외 — 아래 tags 로 분리)
    "http_req_duration{name:login}": ["p(95)<1000"],
    // check 통과율 99% 이상
    checks: ["rate>0.99"],
  },
};

interface CsrfResponse {
  headerName: string;
  token: string;
}

export function setup(): void {
  if (!EMAIL || !PASSWORD) {
    throw new Error(
      "TEST_EMAIL / TEST_PASSWORD 환경변수가 필요합니다. k6/login-load-test/README.md 참고.",
    );
  }
}

export default function (): void {
  // 1) CSRF 토큰 발급. 응답 본문의 {headerName, token} 을 쓰고, CSRF 쿠키는 VU 쿠키자에
  //    자동 저장된다(프론트 getCsrfToken 과 동일한 흐름).
  const csrfRes = http.get(`${BASE_URL}/auth/csrf`, { tags: { name: "csrf" } });
  const csrfOk = check(csrfRes, { "csrf 200": (r) => r.status === 200 });
  if (!csrfOk) {
    sleep(1);
    return;
  }
  const csrf = csrfRes.json() as unknown as CsrfResponse;

  // 2) 로그인. POST 라 CSRF 헤더가 필요하다. 세션 쿠키는 응답에서 자동 저장된다.
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email: EMAIL, password: PASSWORD }),
    {
      headers: {
        "Content-Type": "application/json",
        [csrf.headerName]: csrf.token,
      },
      tags: { name: "login" },
    },
  );

  check(loginRes, {
    "login 200": (r) => r.status === 200,
    "login 응답에 email 포함": (r) => {
      try {
        return r.json("data.email") === EMAIL;
      } catch {
        return false;
      }
    },
  });

  // 실제 사용자처럼 잠깐 쉬어 요청 간격을 준다.
  sleep(1);
}
