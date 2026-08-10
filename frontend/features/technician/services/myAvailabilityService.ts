import { getCsrfToken } from "@/features/shared/api/csrf";
import type { AvailabilityStatus, MyAvailabilitySlot } from "../types/myAvailability";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data?: T;
}

function isEnvelope<T>(value: unknown): value is ApiEnvelope<T> {
  return typeof value === "object" && value !== null && "success" in value;
}

export const myAvailabilityService = {
  /** 날짜별 내 가능 시간 슬롯 목록. GET /api/engineer/me/availability */
  async getMyAvailability(date: string, signal?: AbortSignal): Promise<MyAvailabilitySlot[]> {
    const params = new URLSearchParams({ date });
    const response = await fetch(`${API_BASE_URL}/api/engineer/me/availability?${params}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<MyAvailabilitySlot[]>(body) && body.message
        ? body.message
        : `가능 시간 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<MyAvailabilitySlot[]>(body) || !body.success || !body.data) {
      throw new Error("가능 시간 목록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 슬롯 상태 변경. PATCH /api/engineer/me/availability/{slotId} */
  async updateMyAvailability(slotId: string, status: AvailabilityStatus, signal?: AbortSignal): Promise<MyAvailabilitySlot> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/engineer/me/availability/${slotId}`, {
      method: "PATCH",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: {
        "Content-Type": "application/json",
        [csrf.headerName]: csrf.token,
      },
      body: JSON.stringify({ status }),
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<MyAvailabilitySlot>(body) && body.message
        ? body.message
        : `가능 시간을 변경하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<MyAvailabilitySlot>(body) || !body.success || !body.data) {
      throw new Error("가능 시간 변경 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },
};