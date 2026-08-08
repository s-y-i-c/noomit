import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { repairService } from "../services/repairService";
import type { RepairCase, RepairStatus } from "../types/repair";

const repairApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getMyRepairCases: build.query<RepairCase[], void>({
      queryFn: (_, api) => queryResult(repairService.getMyRepairCases(api.signal)),
      providesTags: [{ type: "RepairCase", id: "MY_LIST" }],
    }),
    getRepairCase: build.query<RepairCase, string>({
      queryFn: (id, api) => queryResult(repairService.getRepairCase(id, api.signal)),
      providesTags: (_result, _error, id) => [{ type: "RepairCase", id }],
    }),
    addRepairDetail: build.mutation<RepairCase, { id: string; description: string; amount: number }>({
      queryFn: ({ id, description, amount }, api) =>
        queryResult(repairService.addDetail(id, description, amount, api.signal)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: "RepairCase", id },
        { type: "RepairCase", id: "MY_LIST" },
      ],
    }),
    deleteRepairDetail: build.mutation<void, { id: string; detailId: string }>({
      queryFn: ({ id, detailId }, api) => queryResult(repairService.deleteDetail(id, detailId, api.signal)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: "RepairCase", id },
        { type: "RepairCase", id: "MY_LIST" },
      ],
    }),
    submitRepairCase: build.mutation<RepairCase, string>({
      queryFn: (id, api) => queryResult(repairService.submit(id, api.signal)),
      invalidatesTags: (_result, _error, id) => [
        { type: "RepairCase", id },
        { type: "RepairCase", id: "MY_LIST" },
      ],
    }),
    getAllRepairCases: build.query<RepairCase[], RepairStatus | undefined>({
      queryFn: (status, api) => queryResult(repairService.getAllRepairCases(status, api.signal)),
      providesTags: [{ type: "RepairCase", id: "ADMIN_LIST" }],
    }),
    approveRepairCase: build.mutation<RepairCase, string>({
      queryFn: (id, api) => queryResult(repairService.approve(id, api.signal)),
      invalidatesTags: [{ type: "RepairCase", id: "ADMIN_LIST" }],
    }),
    rejectRepairCase: build.mutation<RepairCase, { id: string; reason: string }>({
      queryFn: ({ id, reason }, api) => queryResult(repairService.reject(id, reason, api.signal)),
      invalidatesTags: [{ type: "RepairCase", id: "ADMIN_LIST" }],
    }),
  }),
});

export const {
  useGetMyRepairCasesQuery,
  useGetRepairCaseQuery,
  useAddRepairDetailMutation,
  useDeleteRepairDetailMutation,
  useSubmitRepairCaseMutation,
  useGetAllRepairCasesQuery,
  useApproveRepairCaseMutation,
  useRejectRepairCaseMutation,
} = repairApi;
