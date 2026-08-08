import { getCsrfToken } from "@/features/shared/api/csrf";
import type { RepairCase, RepairStatus } from "../types/repair";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data?: T;
}

function isEnvelope<T>(value: unknown): value is ApiEnvelope<T> {
  return typeof value === "object" && value !== null && "success" in value;
}

async function extractData<T>(response: Response, fallbackMessage: string): Promise<T> {
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) {
    const message = isEnvelope<T>(body) && body.message ? body.message : `${fallbackMessage} (${response.status})`;
    throw new Error(message);
  }
  if (!isEnvelope<T>(body) || !body.success || body.data === undefined) {
    throw new Error(`응답 형식이 올바르지 않습니다.`);
  }
  return body.data;
}

export const repairService = {
  // ── 기사용 ──────────────────────────────────────────────────────────────

  async getMyRepairCases(signal?: AbortSignal): Promise<RepairCase[]> {
    const response = await fetch(`${API_BASE_URL}/api/repair-cases/my`, {
      credentials: "include",
      cache: "no-store",
      signal,
    });
    return extractData<RepairCase[]>(response, "수리 케이스 목록을 불러오지 못했습니다.");
  },

  async getRepairCase(id: string, signal?: AbortSignal): Promise<RepairCase> {
    const response = await fetch(`${API_BASE_URL}/api/repair-cases/${id}`, {
      credentials: "include",
      cache: "no-store",
      signal,
    });
    return extractData<RepairCase>(response, "수리 케이스를 불러오지 못했습니다.");
  },

  async addDetail(id: string, description: string, amount: number, signal?: AbortSignal): Promise<RepairCase> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/repair-cases/${id}/details`, {
      method: "POST",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: { "Content-Type": "application/json", [csrf.headerName]: csrf.token },
      body: JSON.stringify({ description, amount }),
    });
    return extractData<RepairCase>(response, "수리 내역을 추가하지 못했습니다.");
  },

  async deleteDetail(id: string, detailId: string, signal?: AbortSignal): Promise<void> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/repair-cases/${id}/details/${detailId}`, {
      method: "DELETE",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: { [csrf.headerName]: csrf.token },
    });
    if (!response.ok) {
      const body: unknown = await response.json().catch(() => null);
      const message = isEnvelope<void>(body) && body.message ? body.message : `수리 내역을 삭제하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
  },

  async submit(id: string, signal?: AbortSignal): Promise<RepairCase> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/repair-cases/${id}/submit`, {
      method: "PUT",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: { [csrf.headerName]: csrf.token },
    });
    return extractData<RepairCase>(response, "제출하지 못했습니다.");
  },

  // ── 관리자용 ─────────────────────────────────────────────────────────────

  async getAllRepairCases(status?: RepairStatus, signal?: AbortSignal): Promise<RepairCase[]> {
    const params = status ? `?status=${status}` : "";
    const response = await fetch(`${API_BASE_URL}/api/repair-cases${params}`, {
      credentials: "include",
      cache: "no-store",
      signal,
    });
    return extractData<RepairCase[]>(response, "수리 케이스 목록을 불러오지 못했습니다.");
  },

  async approve(id: string, signal?: AbortSignal): Promise<RepairCase> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/repair-cases/${id}/approve`, {
      method: "PUT",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: { [csrf.headerName]: csrf.token },
    });
    return extractData<RepairCase>(response, "승인하지 못했습니다.");
  },

  async reject(id: string, reason: string, signal?: AbortSignal): Promise<RepairCase> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/repair-cases/${id}/reject`, {
      method: "PUT",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: { "Content-Type": "application/json", [csrf.headerName]: csrf.token },
      body: JSON.stringify({ reason }),
    });
    return extractData<RepairCase>(response, "반려하지 못했습니다.");
  },
};
