import { getCsrfToken } from "@/features/shared/api/csrf";
import type { Customer, CustomerFilters, CustomerPageData, CustomerStatus } from "../types/customer";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data?: T;
}

function isEnvelope<T>(value: unknown): value is ApiEnvelope<T> {
  return typeof value === "object" && value !== null && "success" in value;
}

export const customerService = {
  async getCustomers(filters: CustomerFilters, signal?: AbortSignal): Promise<CustomerPageData> {
    const params = new URLSearchParams({
      page: String(filters.page),
      size: String(filters.size),
      sort: filters.sort,
    });
    if (filters.keyword) params.set("keyword", filters.keyword);
    if (filters.status) params.set("status", filters.status);

    const response = await fetch(`${API_BASE_URL}/api/customers?${params}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<CustomerPageData>(body) && body.message
        ? body.message
        : `고객 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<CustomerPageData>(body) || !body.success || !body.data) {
      throw new Error("고객 목록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  async getCustomerById(id: string, signal?: AbortSignal): Promise<Customer> {
    const response = await fetch(`${API_BASE_URL}/api/customers/${id}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<Customer>(body) && body.message
        ? body.message
        : `고객 상세 정보를 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<Customer>(body) || !body.success || !body.data) {
      throw new Error("고객 상세 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 관리자 전용 상태 변경. PUT /api/v1/admin/customers/{id}/status — CSRF 보호 대상. */
  async changeStatus(id: string, status: CustomerStatus, signal?: AbortSignal): Promise<void> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/v1/admin/customers/${id}/status`, {
      method: "PUT",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: {
        // 백엔드가 @RequestBody String으로 받는데, Spring이 이 경우 Jackson이 아니라
        // StringHttpMessageConverter로 원본 바이트를 그대로 문자열에 담는다.
        // JSON으로 감싸 보내면 따옴표가 안 벗겨지고 그대로 들어가므로 순수 텍스트로 보낸다.
        "Content-Type": "text/plain",
        [csrf.headerName]: csrf.token,
      },
      body: status,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<void>(body) && body.message
        ? body.message
        : `상태를 변경하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
  },
};
