// k6 공용 인증 헬퍼.
//
// 보호 엔드포인트(/api/**) 부하테스트용. VU당 로그인을 1회만 수행하고(BCrypt 1회),
// 받은 세션(JSESSIONID)을 캐시해 이후 요청에 재사용하게 한다.
//
// 이렇게 하는 이유:
//  1. 매 iteration 로그인하면 BCrypt 해싱이 CPU를 먹어 "타겟 엔드포인트"가 아니라
//     "로그인 CPU"를 측정하게 된다. VU당 1회로 제한해 이 오염을 없앤다.
//  2. k6 쿠키자는 iteration 마다 리셋되므로, 반환된 sessionId 를 요청의 cookies 로
//     직접 실어줘야 세션이 유지된다(안 그러면 401 + 서버에 쓸모없는 세션 누적).
//
// 사용 예:
//   import { ensureSession } from "../lib/auth.ts";
//   export default function () {
//     const sid = ensureSession(BASE_URL, EMAIL, PASSWORD);
//     http.get(`${BASE_URL}/api/repair-cases`, { cookies: { JSESSIONID: sid } });
//   }

import http from "k6/http";
import { check } from "k6";

interface CsrfResponse {
  headerName: string;
  token: string;
}

/**
 * 로그인 1회를 수행하고 세션(JSESSIONID)을 반환한다(캐시 없음). 실패 시 빈 문자열.
 * setup() 에서 한 번 로그인해 모든 VU가 세션을 공유하는 고VU 처리량 테스트 등에 쓴다.
 */
export function login(
  baseUrl: string,
  email: string,
  password: string,
): string {
  const base = baseUrl.replace(/\/+$/, "");
  const csrfRes = http.get(`${base}/auth/csrf`, { tags: { name: "csrf" } });
  if (csrfRes.status !== 200) return "";
  const csrf = csrfRes.json() as unknown as CsrfResponse;

  const loginRes = http.post(
    `${base}/auth/login`,
    JSON.stringify({ email, password }),
    {
      headers: {
        "Content-Type": "application/json",
        [csrf.headerName]: csrf.token,
      },
      tags: { name: "login" },
    },
  );
  if (!check(loginRes, { "login 200": (r) => r.status === 200 })) return "";
  const jsession = loginRes.cookies["JSESSIONID"];
  return jsession && jsession.length > 0 ? jsession[0].value : "";
}

// 모듈 스코프는 VU마다 독립 — VU당 세션을 한 번만 만들어 캐시한다.
let cachedSessionId = "";

/**
 * VU 최초 1회만 로그인하고 세션(JSESSIONID)을 캐시한다. 이후 호출은 캐시값을 그대로 반환.
 * 로그인 실패 시 빈 문자열을 돌려주므로, 호출부에서 falsy 검사로 건너뛰면 된다.
 */
export function ensureSession(
  baseUrl: string,
  email: string,
  password: string,
): string {
  if (!cachedSessionId) cachedSessionId = login(baseUrl, email, password);
  return cachedSessionId;
}

/** 세션 캐시를 비운다(다음 호출 시 다시 로그인). 보통은 쓸 일이 없다. */
export function resetSession(): void {
  cachedSessionId = "";
}
