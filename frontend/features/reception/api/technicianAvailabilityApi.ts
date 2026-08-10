import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { technicianAvailabilityService } from "../services/technicianAvailabilityService";
import type { AvailabilityTimeSlot, AvailableTechnicianResponse } from "../types/serviceRequest";

const technicianAvailabilityApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getAvailableDates: build.query<string[], void>({
      queryFn: (_arg, api) => queryResult(technicianAvailabilityService.getAvailableDates(api.signal)),
    }),
    getAvailabilitySlots: build.query<AvailabilityTimeSlot[], string>({
      queryFn: (date, api) => queryResult(technicianAvailabilityService.getAvailabilitySlots(date, api.signal)),
    }),
    getAvailableTechnicians: build.query<AvailableTechnicianResponse[], { date: string; startTime: string; endTime: string }>({
      queryFn: ({ date, startTime, endTime }, api) =>
        queryResult(technicianAvailabilityService.getAvailableTechnicians(date, startTime, endTime, api.signal)),
    }),
  }),
});

export const {
  useGetAvailableDatesQuery,
  useGetAvailabilitySlotsQuery,
  useGetAvailableTechniciansQuery,
} = technicianAvailabilityApi;