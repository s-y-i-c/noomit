export interface Category {
  id: string;
  name: string;
}

export interface SubCategory {
  id: string;
  categoryId: string;
  name: string;
}

export type ProductStatus = "ACTIVE" | "INACTIVE";

export interface Product {
  id: string;
  categoryId: number;
  subCategoryId: number;
  modelName: string;
  modelCode: string;
  memo: string;
  status: ProductStatus;
}

export interface RegisterProductRequest {
  subCategoryId: number;
  modelName: string;
  modelCode: string;
  memo: string;
}

export interface ProductFilters {
  keyword: string;
  categoryId: string;
  subCategoryId: string;
  status: ProductStatus | "";
  page: number;
  size: number;
  sort: string;
}

export interface ProductPageData {
  products: Product[];
  page: number;
  totalElements: number;
  totalPages: number;
}
