import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { serviceRequestService } from "../services/serviceRequestService";
import type {
  AssignServiceRequestRequest,
  AssignServiceRequestResponse,
  CreateServiceRequestRequest,
  ServiceRequestCreateResponse,
  ServiceRequestFilters,
  ServiceRequestPageData,
} from "../types/serviceRequest";

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
    createServiceRequest: build.mutation<ServiceRequestCreateResponse, CreateServiceRequestRequest>({
      queryFn: (request, api) => queryResult(serviceRequestService.createServiceRequest(request, api.signal)),
      invalidatesTags: [{ type: "ServiceRequest", id: "LIST" }],
    }),
    assignServiceRequest: build.mutation<AssignServiceRequestResponse, { id: string; request: AssignServiceRequestRequest }>({
      queryFn: ({ id, request }, api) => queryResult(serviceRequestService.assignServiceRequest(id, request, api.signal)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: "ServiceRequest", id },
        { type: "ServiceRequest", id: "LIST" },
      ],
    }),
  }),
});

export const {
  useGetServiceRequestsQuery,
  useCreateServiceRequestMutation,
  useAssignServiceRequestMutation,
} = serviceRequestApi;