import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { serviceRequestService } from "../services/serviceRequestService";
import type { ServiceRequestFilters, ServiceRequestPageData } from "../types/serviceRequest";

const serviceRequestApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getServiceRequests: build.query<ServiceRequestPageData, ServiceRequestFilters>({
      queryFn: (filters, api) => queryResult(serviceRequestService.getServiceRequests(filters, api.signal)),
      providesTags: (result) =>
        result
          ? [
              ...result.content.map((item) => ({ type: "ServiceRequest" as const, id: item.id })),
              { type: "ServiceRequest" as const, id: "LIST" },
            ]
          : [{ type: "ServiceRequest" as const, id: "LIST" }],
    }),
  }),
});

export const { useGetServiceRequestsQuery } = serviceRequestApi;