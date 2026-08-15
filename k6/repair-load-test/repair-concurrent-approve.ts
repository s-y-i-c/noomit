// RepairCase 동시 승인 동시성 테스트.
//
// 동일한 SUBMITTED 상태 RepairCase 하나에 여러 VU가 동시에 approve를 시도한다.
// 낙관적 락(@Version)이 올바르게 동작한다면:
//   - 정확히 1건만 200 OK (COMPLETED 전환 성공)
//   - 나머지는 4xx (버전 충돌 or 이미 처리된 상태)
// 500이 1건이라도 나오면 데이터 정합성 위험 신호 → 버그.
//
// setup()이 자동으로 SUBMITTED 케이스를 찾는다. 없으면 테스트 중단.
// → 실행 전 SUBMITTED 상태 케이스를 DB에 미리 만들어두어야 한다.
//
// 실행 예시:
//   k6 run \
//     -e BASE_URL=https://noomit.abcganada.xyz \
//     -e TEST_EMAIL=<어드민 계정> \
//     -e TEST_PASSWORD=<비번> \
//     -e CONCURRENT_VUS=10 \
//     k6/repair-load-test/repair-concurrent-approve.ts

import http from "k6/http";
import { check } from "k6";
import type { Options } from "k6/options";
import { login } from "../lib/auth.ts";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(/\/+$/, "");
const EMAIL = __ENV.TEST_EMAIL || "";
const PASSWORD = __ENV.TEST_PASSWORD || "";
const CONCURRENT_VUS = Number(__ENV.CONCURRENT_VUS || 10);

// shared-iterations: CONCURRENT_VUS개 VU가 CONCURRENT_VUS번의 iteration을
// 나눠 처리한다. VU당 1회씩 → 전부 동시에 approve 시도.
export const options: Options = {
  scenarios: {
    concurrent_approve: {
      executor: "shared-iterations",
      vus: CONCURRENT_VUS,
      iterations: CONCURRENT_VUS,
      maxDuration: "30s",
    },
  },
  thresholds: {
    "checks{check:500 없음}": ["rate==1.00"],
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

  // 1) 어드민 로그인 (1회) — auth.ts 공용 헬퍼 사용
  const sid = login(BASE_URL, EMAIL, PASSWORD);
  if (!sid) throw new Error("로그인 실패 — 계정/비번 확인");

  // 2) SUBMITTED 케이스 자동 탐색
  const listRes = http.get(`${BASE_URL}/api/repair-cases?status=SUBMITTED`, {
    cookies: { JSESSIONID: sid },
  });
  if (listRes.status !== 200) throw new Error(`케이스 목록 조회 실패: ${listRes.status}`);

  const body = listRes.json() as unknown as ApiResponse<RepairCase[]>;
  const cases = body?.data ?? [];
  if (!Array.isArray(cases) || cases.length === 0) {
    throw new Error("SUBMITTED 상태인 RepairCase가 없습니다. 테스트 전에 수리 케이스를 제출 상태로 만들어두세요.");
  }
  const caseId = cases[0].id;

  // 3) approve용 CSRF 발급
  const csrfRes = http.get(`${BASE_URL}/auth/csrf`, {
    cookies: { JSESSIONID: sid },
  });
  if (csrfRes.status !== 200) throw new Error("CSRF 발급 실패");
  const csrf = csrfRes.json() as unknown as CsrfResponse;

  console.log(`[setup] 대상 케이스: ${caseId} | VU 수: ${CONCURRENT_VUS}`);
  return { sid, csrfHeader: csrf.headerName, csrfToken: csrf.token, caseId };
}

export default function (data: SetupData): void {
  const { sid, csrfHeader, csrfToken, caseId } = data;

  const res = http.put(
    `${BASE_URL}/api/repair-cases/${caseId}/approve`,
    null,
    {
      cookies: { JSESSIONID: sid },
      headers: { [csrfHeader]: csrfToken },
      tags: { name: "concurrent-approve" },
    },
  );

  // 200 = 승인 성공 (딱 1건이어야 함)
  // 4xx = 충돌·이미 처리됨 (낙관적 락 또는 상태 검증이 막아준 것)
  // 500 = 서버 오류 → 데이터 정합성 위험
  check(res, {
    "500 없음": (r) => r.status !== 500,
    "200 또는 4xx": (r) => r.status === 200 || (r.status >= 400 && r.status < 500),
  });

  console.log(`VU ${__VU}: status=${res.status}`);
}
