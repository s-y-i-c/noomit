import { getCsrfToken } from "@/features/shared/api/csrf";
import type {
  Category,
  Product,
  ProductFilters,
  ProductPageData,
  ProductStatus,
  RegisterProductRequest,
  SubCategory,
} from "../types/product";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data?: T;
}

function isEnvelope<T>(value: unknown): value is ApiEnvelope<T> {
  return typeof value === "object" && value !== null && "success" in value;
}

export const productService = {
  async getCategories(signal?: AbortSignal): Promise<Category[]> {
    const response = await fetch(`${API_BASE_URL}/api/products/categories`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<Category[]>(body) && body.message
        ? body.message
        : `카테고리 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<Category[]>(body) || !body.success || !body.data) {
      throw new Error("카테고리 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  async getSubCategories(signal?: AbortSignal): Promise<SubCategory[]> {
    const response = await fetch(`${API_BASE_URL}/api/products/subcategories`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<SubCategory[]>(body) && body.message
        ? body.message
        : `서브카테고리 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<SubCategory[]>(body) || !body.success || !body.data) {
      throw new Error("서브카테고리 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  async getProducts(filters: ProductFilters, signal?: AbortSignal): Promise<ProductPageData> {
    const params = new URLSearchParams({
      page: String(filters.page),
      size: String(filters.size),
      sort: filters.sort,
    });
    if (filters.keyword) params.set("keyword", filters.keyword);
    if (filters.categoryId) params.set("categoryId", filters.categoryId);
    if (filters.subCategoryId) params.set("subCategoryId", filters.subCategoryId);
    if (filters.status) params.set("status", filters.status);

    const response = await fetch(`${API_BASE_URL}/api/products?${params}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<ProductPageData>(body) && body.message
        ? body.message
        : `제품 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<ProductPageData>(body) || !body.success || !body.data) {
      throw new Error("제품 목록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  async getProductById(id: string, signal?: AbortSignal): Promise<Product> {
    const response = await fetch(`${API_BASE_URL}/api/products/${id}`, {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<Product>(body) && body.message
        ? body.message
        : `제품 상세 정보를 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<Product>(body) || !body.success || !body.data) {
      throw new Error("제품 상세 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 관리자 전용 상태 변경. PUT /api/admin/products/{id}/status — CSRF 보호 대상. */
  async changeStatus(id: string, status: ProductStatus, signal?: AbortSignal): Promise<void> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/admin/products/${id}/status`, {
      method: "PUT",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: {
        // 백엔드가 @RequestBody String으로 받는데, Spring이 이 경우 Jackson이 아니라
        // StringHttpMessageConverter로 원본 바이트를 그대로 문자열에 담는다.
        // JSON으로 감싸 보내면 따옴표가 안 벗겨지고 그대로 들어가므로 순수 텍스트로 보낸다.
        "Content-Type": "text/plain",
        [csrf.headerName]: csrf.token,
      },
      body: status,
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<void>(body) && body.message
        ? body.message
        : `상태를 변경하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
  },

  /** 관리자 전용 제품 등록. POST /api/admin/products — CSRF 보호 대상. */
  async registerProduct(request: RegisterProductRequest, signal?: AbortSignal): Promise<Product> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(`${API_BASE_URL}/api/admin/products`, {
      method: "POST",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: {
        "Content-Type": "application/json",
        [csrf.headerName]: csrf.token,
      },
      body: JSON.stringify(request),
    });
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message = isEnvelope<Product>(body) && body.message
        ? body.message
        : `제품을 등록하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<Product>(body) || !body.success || !body.data) {
      throw new Error("제품 등록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },
};
