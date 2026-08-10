import type { MyAssignedRequest, MyAssignedRequestDetail } from "../types/assignedRequest";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data?: T;
}

function isEnvelope<T>(value: unknown): value is ApiEnvelope<T> {
  return typeof value === "object" && value !== null && "success" in value;
}

export const assignedRequestService = {
  /** 날짜별 내 배정 목록. GET /api/engineer/me/requests */
  async getMyAssignedRequests(date: string, signal?: AbortSignal): Promise<MyAssignedRequest[]> {
    const params = new URLSearchParams({ date });
    const response = await fetch(`${API_BASE_URL}/api/engineer/me/requests?${params}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<MyAssignedRequest[]>(body) && body.message
        ? body.message
        : `배정 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<MyAssignedRequest[]>(body) || !body.success || !body.data) {
      throw new Error("배정 목록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 배정 상세. GET /api/engineer/me/requests/{requestId} */
  async getMyAssignedRequestDetail(requestId: string, signal?: AbortSignal): Promise<MyAssignedRequestDetail> {
    const response = await fetch(`${API_BASE_URL}/api/engineer/me/requests/${requestId}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<MyAssignedRequestDetail>(body) && body.message
        ? body.message
        : `배정 상세를 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<MyAssignedRequestDetail>(body) || !body.success || !body.data) {
      throw new Error("배정 상세 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },
};