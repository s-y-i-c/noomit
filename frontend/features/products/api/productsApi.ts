import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { productService } from "../services/productService";
import type {
  Category,
  Product,
  ProductFilters,
  ProductPageData,
  ProductStatus,
  RegisterProductRequest,
  SubCategory,
} from "../types/product";

const productsApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getCategories: build.query<Category[], void>({
      queryFn: (_arg, api) => queryResult(productService.getCategories(api.signal)),
    }),
    getSubCategories: build.query<SubCategory[], void>({
      queryFn: (_arg, api) => queryResult(productService.getSubCategories(api.signal)),
    }),
    getProducts: build.query<ProductPageData, ProductFilters>({
      queryFn: (filters, api) => queryResult(productService.getProducts(filters, api.signal)),
      providesTags: (result) =>
        result
          ? [
              ...result.products.map((product) => ({ type: "Product" as const, id: product.id })),
              { type: "Product" as const, id: "LIST" },
            ]
          : [{ type: "Product" as const, id: "LIST" }],
    }),
    getProductById: build.query<Product, string>({
      queryFn: (id, api) => queryResult(productService.getProductById(id, api.signal)),
      providesTags: (_result, _error, id) => [{ type: "Product", id }],
    }),
    registerProduct: build.mutation<Product, RegisterProductRequest>({
      queryFn: (request, api) => queryResult(productService.registerProduct(request, api.signal)),
      invalidatesTags: [{ type: "Product", id: "LIST" }],
    }),
    changeProductStatus: build.mutation<void, { id: string; status: ProductStatus }>({
      queryFn: ({ id, status }, api) => queryResult(productService.changeStatus(id, status, api.signal)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: "Product", id },
        { type: "Product", id: "LIST" },
      ],
    }),
  }),
});

export const {
  useGetCategoriesQuery,
  useGetSubCategoriesQuery,
  useGetProductsQuery,
  useGetProductByIdQuery,
  useRegisterProductMutation,
  useChangeProductStatusMutation,
} = productsApi;
