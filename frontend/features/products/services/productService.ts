import { getCsrfToken } from "@/features/shared/api/csrf";
import type { Category, Product, RegisterProductRequest, SubCategory } from "../types/product";

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
