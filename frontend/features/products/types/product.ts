export interface Category {
  id: string;
  name: string;
}

export interface SubCategory {
  id: string;
  categoryId: string;
  name: string;
}

export interface Product {
  id: string;
  categoryId: number;
  subCategoryId: number;
  modelName: string;
  modelCode: string;
  memo: string;
}

export interface RegisterProductRequest {
  subCategoryId: number;
  modelName: string;
  modelCode: string;
  memo: string;
}
