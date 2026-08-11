import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { myAvailabilityService } from "../services/myAvailabilityService";
import type { AvailabilityStatus, MyAvailabilitySlot } from "../types/myAvailability";

const myAvailabilityApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getMyAvailability: build.query<MyAvailabilitySlot[], string>({
      queryFn: (date, api) => queryResult(myAvailabilityService.getMyAvailability(date, api.signal)),
    }),
    updateMyAvailability: build.mutation<MyAvailabilitySlot, { slotId: string; status: AvailabilityStatus; date: string }>({
      queryFn: ({ slotId, status }, api) =>
        queryResult(myAvailabilityService.updateMyAvailability(slotId, status, api.signal)),
      // 낙관적 업데이트
      async onQueryStarted({ slotId, status, date }, { dispatch, queryFulfilled }) {
        const patch = dispatch(
          myAvailabilityApi.util.updateQueryData("getMyAvailability", date, (draft) => {
            const slot = draft.find((s) => s.slotId === slotId);
            if (slot) slot.status = status;
          }),
        );
        try {
          await queryFulfilled;
        } catch {
          patch.undo();
        }
      },
    }),
  }),
});

export const {
  useGetMyAvailabilityQuery,
  useUpdateMyAvailabilityMutation,
} = myAvailabilityApi;