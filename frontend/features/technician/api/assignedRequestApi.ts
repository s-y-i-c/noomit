import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { assignedRequestService } from "../services/assignedRequestService";
import type { MyAssignedRequest, MyAssignedRequestDetail } from "../types/assignedRequest";

const assignedRequestApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getMyAssignedRequests: build.query<MyAssignedRequest[], string>({
      queryFn: (date, api) => queryResult(assignedRequestService.getMyAssignedRequests(date, api.signal)),
    }),
    getMyAssignedRequestDetail: build.query<MyAssignedRequestDetail, string>({
      queryFn: (requestId, api) => queryResult(assignedRequestService.getMyAssignedRequestDetail(requestId, api.signal)),
    }),
  }),
});

export const {
  useGetMyAssignedRequestsQuery,
  useGetMyAssignedRequestDetailQuery,
} = assignedRequestApi;