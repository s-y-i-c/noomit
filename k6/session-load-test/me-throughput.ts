// 인증된 요청(/api/me) 고VU 처리량 테스트.
//
// setup 에서 딱 1번 로그인해 세션을 만들고, 모든 VU가 그 세션을 공유해 /api/me 만 때린다.
// → 테스트 중 로그인(BCrypt)이 0회라, 로그인 CPU 오염 없이 순수하게 "동시 요청을 서버가
//   몇까지 버티나(동시성 한계)"를 측정한다. (VU당 로그인하는 session-throughput.ts 와 대비)
//
// 단계적으로 올린다: -e PEAK_VUS=100 → 200 → ... 관측하며 무너지는 지점을 찾는다.
// 관측: CPU, 톰캣 스레드, /me p95, 실패율. (힙/세션은 병목 아님이 이미 확인됨)
//   k6 run -e BASE_URL=... -e TEST_EMAIL=... -e TEST_PASSWORD=... -e PEAK_VUS=100 me-throughput.ts

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
const PEAK_VUS = Number(__ENV.PEAK_VUS || 100);
// 고VU에서 램프를 길게 줘야 서버가 계단식으로 적응한다.
const RAMP = __ENV.RAMP || "1m";
// think-time. /me 는 워낙 싸서 초당 요청 수를 올리려면 이 값을 줄인다(예: 0.1).
const SLEEP = Number(__ENV.SLEEP || 1);

export const options: Options = {
  stages: [
    { duration: RAMP, target: PEAK_VUS }, // 램프업
    { duration: "1m", target: PEAK_VUS }, // 유지(측정 구간)
    { duration: "20s", target: 0 }, // 램프다운
  ],
  thresholds: {
    http_req_failed: ["rate<0.05"],
    "http_req_duration{name:me}": ["p(95)<1000"],
  },
};

export function setup(): { sid: string } {
  if (!EMAIL || !PASSWORD) {
    throw new Error("TEST_EMAIL / TEST_PASSWORD 환경변수가 필요합니다.");
  }
  const sid = login(BASE_URL, EMAIL, PASSWORD);
  if (!sid) throw new Error("setup 로그인 실패 — 계정/BASE_URL 확인");
  return { sid };
}

export default function (data: { sid: string }): void {
  // 모든 VU가 공유 세션으로 인증된 요청만 반복한다.
  const res = http.get(`${BASE_URL}/api/me`, {
    cookies: { JSESSIONID: data.sid },
    tags: { name: "me" },
  });
  check(res, { "me 200": (r) => r.status === 200 });
  sleep(SLEEP);
}
