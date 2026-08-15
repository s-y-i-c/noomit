// 인증된 쓰기 요청(POST /api/customers) 고VU 처리량 테스트.
//
// setup 에서 로그인 1회 + CSRF 토큰 1회만 발급받아 모든 VU가 공유한다.
// → 로그인(BCrypt) CPU 오염 없이 순수하게 "동시 등록 요청을 서버가 몇까지 버티나"를 측정.
// (session-load-test/me-throughput.ts, customer-load-test/customers-phone-throughput.ts 와 동일 패턴)
//
//   k6 run -e BASE_URL=... -e TEST_EMAIL=... -e TEST_PASSWORD=... -e TEST_PHONE=01000000099 -e PEAK_VUS=20 -e SLEEP=0.3 customers-register-throughput.ts

import http from "k6/http";
import { check, sleep } from "k6";
import type { Options } from "k6/options";
import { login } from "../lib/auth.ts";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(
  /\/+$/,
  "",
);
const EMAIL = __ENV.TEST_EMAIL || "";
const PASSWORD = __ENV.TEST_PASSWORD || "";
// 사전에 GET /api/customers/phone 으로 "신규 유저입니다"(미존재) 확인된 번호를 기본값으로 둔다.
const PHONE = __ENV.TEST_PHONE || "01000000099";
const PEAK_VUS = Number(__ENV.PEAK_VUS || 20);
const RAMP = __ENV.RAMP || "30s";
const SLEEP = Number(__ENV.SLEEP || 1);

export const options: Options = {
  stages: [
    { duration: RAMP, target: PEAK_VUS },
    { duration: "1m", target: PEAK_VUS },
    { duration: "20s", target: 0 },
  ],
  thresholds: {
    http_req_failed: ["rate<0.05"],
    "http_req_duration{name:customers-register}": ["p(95)<1000"],
  },
};

interface CsrfResponse {
  headerName: string;
  token: string;
}

export function setup(): {
  sid: string;
  csrfHeader: string;
  csrfToken: string;
  csrfCookie: string;
} {
  if (!EMAIL || !PASSWORD) {
    throw new Error("TEST_EMAIL / TEST_PASSWORD 환경변수가 필요합니다.");
  }
  const sid = login(BASE_URL, EMAIL, PASSWORD);
  if (!sid) throw new Error("setup 로그인 실패 — 계정/BASE_URL 확인");

  // CSRF 토큰/쿠키를 별도로 한 번 더 받는다 — login() 내부에서 이미 /auth/csrf 를 한 번
  // 호출했기 때문에(auth.ts), 이 VU 쿠키 저장소엔 NOOMIT-XSRF-TOKEN 이 이미 들어있다.
  // 그 상태로 /auth/csrf 를 다시 부르면 서버가 "이미 있다"고 판단해 Set-Cookie 를 다시
  // 안 보낸다(res.cookies 가 빈 객체로 옴) — 그래서 응답이 아니라 VU 쿠키 저장소(jar)에서
  // 직접 읽어야 한다.
  const csrfRes = http.get(`${BASE_URL}/auth/csrf`);
  const csrf = csrfRes.json() as unknown as CsrfResponse;
  const jarCookies = http.cookieJar().cookiesForURL(`${BASE_URL}/`);
  const csrfCookie = jarCookies["NOOMIT-XSRF-TOKEN"]?.[0];
  if (!csrf?.token || !csrfCookie) throw new Error("setup CSRF 발급 실패");

  return {
    sid,
    csrfHeader: csrf.headerName,
    csrfToken: csrf.token,
    csrfCookie,
  };
}

export default function (data: {
  sid: string;
  csrfHeader: string;
  csrfToken: string;
  csrfCookie: string;
}): void {
  const res = http.post(
    `${BASE_URL}/api/customers`,
    JSON.stringify({
      name: "k6부하테스트",
      phoneNumber: PHONE,
      zipCode: "00000",
      address: "k6 load test",
      detailAddress: "delete-me",
      memo: "k6 register load test - safe to delete",
    }),
    {
      cookies: { JSESSIONID: data.sid, "NOOMIT-XSRF-TOKEN": data.csrfCookie },
      headers: {
        "Content-Type": "application/json",
        [data.csrfHeader]: data.csrfToken,
      },
      tags: { name: "customers-register" },
    },
  );
  check(res, { "register 200": (r) => r.status === 200 });
  sleep(SLEEP);
}
