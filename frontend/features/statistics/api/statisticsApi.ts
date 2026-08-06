import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { statisticsService } from "../services/statisticsService";
import type { StatisticsDashboardData, StatisticsFilters } from "../types/statistics";

const statisticsApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getStatisticsDashboard: build.query<StatisticsDashboardData, StatisticsFilters>({
      queryFn: (filters, api) => queryResult(statisticsService.getDashboard(filters, api.signal)),
    }),
  }),
});

export const { useLazyGetStatisticsDashboardQuery } = statisticsApi;
