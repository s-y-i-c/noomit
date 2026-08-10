import type { AvailabilityTimeSlot, AvailableTechnicianResponse } from "../types/serviceRequest";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data?: T;
}

function isEnvelope<T>(value: unknown): value is ApiEnvelope<T> {
  return typeof value === "object" && value !== null && "success" in value;
}

export const technicianAvailabilityService = {
  /** 실제 배정 가능한 날짜 목록. GET /api/counselor/reception/technicians/availability/dates */
  async getAvailableDates(signal?: AbortSignal): Promise<string[]> {
    const response = await fetch(`${API_BASE_URL}/api/counselor/reception/technicians/availability/dates`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<string[]>(body) && body.message
        ? body.message
        : `배정 가능한 날짜 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<string[]>(body) || !body.success || !body.data) {
      throw new Error("날짜 목록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 날짜별 시간대 가용 요약. GET /api/counselor/reception/technicians/availability/slots */
  async getAvailabilitySlots(date: string, signal?: AbortSignal): Promise<AvailabilityTimeSlot[]> {
    const params = new URLSearchParams({ date });
    const response = await fetch(`${API_BASE_URL}/api/counselor/reception/technicians/availability/slots?${params}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<AvailabilityTimeSlot[]>(body) && body.message
        ? body.message
        : `시간대 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<AvailabilityTimeSlot[]>(body) || !body.success || !body.data) {
      throw new Error("시간대 목록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 특정 슬롯에 가능한 기사 목록. GET /api/counselor/reception/technicians/availability */
  async getAvailableTechnicians(
    date: string,
    startTime: string,
    endTime: string,
    signal?: AbortSignal,
  ): Promise<AvailableTechnicianResponse[]> {
    const params = new URLSearchParams({ date, startTime, endTime });
    const response = await fetch(`${API_BASE_URL}/api/counselor/reception/technicians/availability?${params}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<AvailableTechnicianResponse[]>(body) && body.message
        ? body.message
        : `가능한 기사 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<AvailableTechnicianResponse[]>(body) || !body.success || !body.data) {
      throw new Error("기사 목록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },
};