import type { StatisticsDashboardData, StatisticsFilters } from "../types/statistics";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

interface ApiEnvelope {
  success: boolean;
  message?: string;
  data?: StatisticsDashboardData;
}

function isEnvelope(value: unknown): value is ApiEnvelope {
  return typeof value === "object" && value !== null && "success" in value;
}

export const statisticsService = {
  async getDashboard(filters: StatisticsFilters, signal?: AbortSignal): Promise<StatisticsDashboardData> {
    const params = new URLSearchParams({
      from: filters.from,
      to: filters.to,
      repeatWindowDays: String(filters.repeatWindowDays),
    });
    if (filters.technicianId) params.set("technicianId", filters.technicianId);
    if (filters.customerId) params.set("customerId", filters.customerId);
    if (filters.productId) params.set("productId", filters.productId);
    if (filters.status) params.set("status", filters.status);

    const response = await fetch(`${API_BASE_URL}/api/admin/statistics/dashboard?${params}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope(body) && body.message
        ? body.message
        : `통계를 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope(body) || !body.success || !body.data) {
      throw new Error("통계 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },
};
