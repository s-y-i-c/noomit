import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { customerService } from "../services/customerService";
import type { Customer, CustomerFilters, CustomerPageData, CustomerStatus } from "../types/customer";

const customersApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getCustomers: build.query<CustomerPageData, CustomerFilters>({
      queryFn: (filters, api) => queryResult(customerService.getCustomers(filters, api.signal)),
      providesTags: (result) =>
        result
          ? [
              ...result.customers.map((customer) => ({ type: "Customer" as const, id: customer.id })),
              { type: "Customer" as const, id: "LIST" },
            ]
          : [{ type: "Customer" as const, id: "LIST" }],
    }),
    getCustomerById: build.query<Customer, string>({
      queryFn: (id, api) => queryResult(customerService.getCustomerById(id, api.signal)),
      providesTags: (_result, _error, id) => [{ type: "Customer", id }],
    }),
    changeCustomerStatus: build.mutation<void, { id: string; status: CustomerStatus }>({
      queryFn: ({ id, status }, api) => queryResult(customerService.changeStatus(id, status, api.signal)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: "Customer", id },
        { type: "Customer", id: "LIST" },
      ],
    }),
    // 전화번호 조회는 버튼 클릭으로 그때그때 트리거하는 거라 lazy 쿼리로 뺀다.
    findCustomerByPhoneNumber: build.query<Customer | null, string>({
      queryFn: (phoneNumber, api) => queryResult(customerService.findCustomerByPhoneNumber(phoneNumber, api.signal)),
    }),
  }),
});

export const {
  useGetCustomersQuery,
  useGetCustomerByIdQuery,
  useChangeCustomerStatusMutation,
  useLazyFindCustomerByPhoneNumberQuery,
} = customersApi;
