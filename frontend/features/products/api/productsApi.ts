import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { productService } from "../services/productService";
import type { Category, Product, RegisterProductRequest, SubCategory } from "../types/product";

const productsApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getCategories: build.query<Category[], void>({
      queryFn: (_arg, api) => queryResult(productService.getCategories(api.signal)),
    }),
    getSubCategories: build.query<SubCategory[], void>({
      queryFn: (_arg, api) => queryResult(productService.getSubCategories(api.signal)),
    }),
    searchProducts: build.query<Product[], string>({
      queryFn: (keyword, api) => queryResult(productService.searchProducts(keyword, api.signal)),
    }),
    registerProduct: build.mutation<Product, RegisterProductRequest>({
      queryFn: (request, api) => queryResult(productService.registerProduct(request, api.signal)),
    }),
  }),
});

export const {
  useGetCategoriesQuery,
  useGetSubCategoriesQuery,
  useLazySearchProductsQuery,
  useRegisterProductMutation,
} = productsApi;
