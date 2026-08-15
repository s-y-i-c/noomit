// 인증된 요청(GET /api/counselor/reception/requests) 저VU 처리량 테스트 — 공유세션 버전.
//
// 배경: 다른 팀원이 이 엔드포인트를 VU 1→10 스윕으로 테스트했을 때 "VU 7 전후에서 CPU
// 100% 포화"를 관측했다(로그인/목록조회 프로젝션 개선 문서 8번 항목). 그런데 프로젝션으로
// 쿼리 비용을 줄였는데도 이 포화 시점(VU~7)이 개선 전후로 안 움직여서, 목록 쿼리 자체가
// 아니라 "VU/iteration마다 재로그인해서 생기는 BCrypt CPU"가 섞여 측정됐을 가능성이 있다
// (login-load-test 에서 BCrypt cost=8 로도 순수 로그인만으로 VU~10 근처에서 CPU 포화가
// 확인된 바 있음, 스케일이 거의 일치).
//
// 이 스크립트는 setup() 에서 로그인 1회만 하고 모든 VU가 세션을 공유한다 — 그래서 만약
// 저VU(1~10)에서도 CPU 가 여전히 급증한다면 "목록 쿼리 자체의 문제"가 맞고, 반대로
// CPU 가 훨씬 안 오른다면 "로그인 오염"이 범인이었다는 뜻이 된다.
//
// 단계별로 VU 를 바꿔가며 여러 번 실행해서 비교한다(다른 테스트들과 동일한 방식):
//   k6 run -e BASE_URL=... -e TEST_EMAIL=... -e TEST_PASSWORD=... -e PEAK_VUS=7 -e SIZE=20 -e SLEEP=1 reception-list-throughput.ts

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
const SIZE = __ENV.SIZE || "20";
const PEAK_VUS = Number(__ENV.PEAK_VUS || 10);
const RAMP = __ENV.RAMP || "20s";
const SLEEP = Number(__ENV.SLEEP || 1);

export const options: Options = {
  stages: [
    { duration: RAMP, target: PEAK_VUS },
    { duration: "40s", target: PEAK_VUS },
    { duration: "10s", target: 0 },
  ],
  thresholds: {
    http_req_failed: ["rate<0.05"],
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
  // 모든 VU가 공유 세션으로 목록 조회만 반복한다 — 로그인은 setup 에서 1번뿐.
  const res = http.get(
    `${BASE_URL}/api/counselor/reception/requests?size=${SIZE}`,
    {
      cookies: { JSESSIONID: data.sid },
      tags: { name: "reception-list" },
    },
  );
  check(res, { "reception-list 200": (r) => r.status === 200 });
  sleep(SLEEP);
}
