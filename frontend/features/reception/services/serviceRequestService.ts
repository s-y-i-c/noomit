import { getCsrfToken } from "@/features/shared/api/csrf";
import type {
  AssignServiceRequestRequest,
  AssignServiceRequestResponse,
  CancelServiceRequestRequest,
  CancelServiceRequestResponse,
  CreateServiceRequestRequest,
  ReassignServiceRequestRequest,
  ServiceRequestCreateResponse,
  ServiceRequestDetail,
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

  /** 접수 상세 조회. GET /api/counselor/reception/requests/{id} */
  async getServiceRequestDetail(id: string, signal?: AbortSignal): Promise<ServiceRequestDetail> {
    const response = await fetch(`${API_BASE_URL}/api/counselor/reception/requests/${id}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<ServiceRequestDetail>(body) && body.message
        ? body.message
        : `접수 상세를 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<ServiceRequestDetail>(body) || !body.success || !body.data) {
      throw new Error("접수 상세 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 접수 취소. PATCH /api/counselor/reception/requests/{id}/cancel */
  async cancelServiceRequest(id: string, request: CancelServiceRequestRequest, signal?: AbortSignal): Promise<CancelServiceRequestResponse> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/counselor/reception/requests/${id}/cancel`, {
      method: "PATCH",
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
      const message = isEnvelope<CancelServiceRequestResponse>(body) && body.message
        ? body.message
        : `접수를 취소하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<CancelServiceRequestResponse>(body) || !body.success || !body.data) {
      throw new Error("접수 취소 응답 형식이 올바르지 않습니다.");
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

  /** 기사 재배정. POST /api/counselor/reception/requests/{id}/reassign */
  async reassignServiceRequest(id: string, request: ReassignServiceRequestRequest, signal?: AbortSignal): Promise<AssignServiceRequestResponse> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/counselor/reception/requests/${id}/reassign`, {
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
        : `기사를 재배정하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<AssignServiceRequestResponse>(body) || !body.success || !body.data) {
      throw new Error("재배정 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },
};
