/**
 * 다음(Daum) 우편번호 서비스 연동. 백엔드 API도, API 키도 필요 없다 —
 * 클라이언트에서 스크립트를 불러와 팝업을 띄우고, 사용자가 고른 주소를 콜백으로 받는다.
 * https://postcode.map.daum.net/guide
 */

export interface DaumPostcodeResult {
  zonecode: string;
  roadAddress: string;
  jibunAddress: string;
}

interface DaumPostcodeInstance {
  open: () => void;
}

interface DaumPostcodeConstructor {
  new (options: { oncomplete: (data: DaumPostcodeResult) => void }): DaumPostcodeInstance;
}

declare global {
  interface Window {
    daum?: { Postcode: DaumPostcodeConstructor };
  }
}

const SCRIPT_SRC = "https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";

let scriptPromise: Promise<void> | null = null;

/**
 * 스크립트를 미리 불러온다. 클릭 핸들러 안에서 await로 로드부터 하면, 브라우저가
 * "사용자 제스처와 동기적으로 연결되지 않았다"고 판단해 window.open 기반 팝업을 막아버린다.
 * 그래서 컴포넌트 마운트 시점처럼 클릭과 무관한 타이밍에 미리 불러와둬야 한다.
 */
export function preloadDaumPostcode(): Promise<void> {
  if (typeof window === "undefined") {
    return Promise.resolve();
  }
  if (window.daum?.Postcode) {
    return Promise.resolve();
  }
  if (!scriptPromise) {
    scriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement("script");
      script.src = SCRIPT_SRC;
      script.async = true;
      script.onload = () => resolve();
      script.onerror = () => {
        scriptPromise = null;
        reject(new Error("주소 검색 스크립트를 불러오지 못했습니다."));
      };
      document.head.appendChild(script);
    });
  }
  return scriptPromise;
}

/**
 * 이미 로드된 스크립트로 팝업을 "동기적으로" 연다 (사용자 클릭 이벤트 핸들러 안에서 바로 호출해야
 * 팝업 차단에 안 걸린다). 아직 로드가 안 끝났으면 false를 돌려주고 아무 것도 하지 않는다.
 */
export function openDaumPostcode(onComplete: (data: DaumPostcodeResult) => void): boolean {
  if (typeof window === "undefined" || !window.daum?.Postcode) {
    return false;
  }
  new window.daum.Postcode({ oncomplete: onComplete }).open();
  return true;
}
