// 인증된 요청(/api/customers/phone) 고VU 처리량 테스트.
//
// setup 에서 딱 1번 로그인해 세션을 만들고, 모든 VU가 그 세션을 공유해 조회만 반복한다.
// → 테스트 중 로그인(BCrypt)이 0회라, 로그인 CPU 오염 없이 순수하게 "동시 조회를 서버가
//   몇까지 버티나(동시성 한계)"를 측정한다. (session-load-test/me-throughput.ts 와 동일 패턴)
//
// 이 엔드포인트는 ADMIN/ENGINEER/COUNSELOR 권한이 필요하다(CustomerController
// @PreAuthorize) — TEST_EMAIL 계정이 해당 롤을 갖고 있어야 200 이 나온다. /me 와 달리
// DB 를 실제로 치므로(customerService.search), CPU 뿐 아니라 DB(HikariCP) 포화 여부도
// 함께 관찰 대상이다.
//
// 단계적으로 올린다: -e PEAK_VUS=50 → 100 → ... 관측하며 무너지는 지점을 찾는다.
// 관측: CPU, DB 커넥션 풀 대기, p95, 실패율.
//   k6 run -e BASE_URL=... -e TEST_EMAIL=... -e TEST_PASSWORD=... -e TEST_PHONE=... -e PEAK_VUS=50 customers-phone-throughput.ts

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
// 존재 여부와 무관하게 조회 자체는 200 을 반환하므로(신규 유저 응답), 실제 등록된 번호가
// 아니어도 부하테스트는 가능하다. 다만 DB 인덱스 히트를 실측하려면 등록된 번호를 쓴다.
const PHONE = __ENV.TEST_PHONE || "01000000000";
const PEAK_VUS = Number(__ENV.PEAK_VUS || 50);
// 고VU에서 램프를 길게 줘야 서버가 계단식으로 적응한다.
const RAMP = __ENV.RAMP || "1m";
const SLEEP = Number(__ENV.SLEEP || 1);

export const options: Options = {
  stages: [
    { duration: RAMP, target: PEAK_VUS }, // 램프업
    { duration: "1m", target: PEAK_VUS }, // 유지(측정 구간)
    { duration: "20s", target: 0 }, // 램프다운
  ],
  thresholds: {
    http_req_failed: ["rate<0.05"],
    "http_req_duration{name:customers-phone}": ["p(95)<1000"],
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
  // 모든 VU가 공유 세션으로 인증된 조회만 반복한다.
  const res = http.get(`${BASE_URL}/api/customers/phone?phoneNumber=${PHONE}`, {
    cookies: { JSESSIONID: data.sid },
    tags: { name: "customers-phone" },
  });
  check(res, { "customers-phone 200": (r) => r.status === 200 });
  sleep(SLEEP);
}
