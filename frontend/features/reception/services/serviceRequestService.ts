import { getCsrfToken } from "@/features/shared/api/csrf";
import type {
  AssignServiceRequestRequest,
  AssignServiceRequestResponse,
  CreateServiceRequestRequest,
  ServiceRequestCreateResponse,
  ServiceRequestFilters,
  ServiceRequestPageData,
} from "../types/serviceRequest";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data?: T;
}

function isEnvelope<T>(value: unknown): value is ApiEnvelope<T> {
  return typeof value === "object" && value !== null && "success" in value;
}

export const serviceRequestService = {
  async getServiceRequests(filters: ServiceRequestFilters, signal?: AbortSignal): Promise<ServiceRequestPageData> {
    const params = new URLSearchParams({
      page: String(filters.page),
      size: String(filters.size),
      sort: filters.sort,
    });
    if (filters.status) params.set("status", filters.status);

    const response = await fetch(`${API_BASE_URL}/api/counselor/reception/requests?${params}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<ServiceRequestPageData>(body) && body.message
        ? body.message
        : `접수 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<ServiceRequestPageData>(body) || !body.success || !body.data) {
      throw new Error("접수 목록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 접수 생성. POST /api/counselor/reception/requests */
  async createServiceRequest(request: CreateServiceRequestRequest, signal?: AbortSignal): Promise<ServiceRequestCreateResponse> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/counselor/reception/requests`, {
      method: "POST",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: {
        "Content-Type": "application/json",
        [csrf.headerName]: csrf.token,
      },
      body: JSON.stringify(request),
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<ServiceRequestCreateResponse>(body) && body.message
        ? body.message
        : `접수를 생성하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<ServiceRequestCreateResponse>(body) || !body.success || !body.data) {
      throw new Error("접수 생성 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 기사 배정. POST /api/counselor/reception/requests/{id}/assign */
  async assignServiceRequest(id: string, request: AssignServiceRequestRequest, signal?: AbortSignal): Promise<AssignServiceRequestResponse> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/counselor/reception/requests/${id}/assign`, {
      method: "POST",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: {
        "Content-Type": "application/json",
        [csrf.headerName]: csrf.token,
      },
      body: JSON.stringify(request),
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<AssignServiceRequestResponse>(body) && body.message
        ? body.message
        : `기사를 배정하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<AssignServiceRequestResponse>(body) || !body.success || !body.data) {
      throw new Error("배정 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },
};
