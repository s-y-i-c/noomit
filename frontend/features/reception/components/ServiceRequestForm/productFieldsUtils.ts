/**
 * 접수 화면이 이어받는 제품 입력값.
 */
export interface ProductFieldsValue {
  categoryId: string;
  subCategoryId: string;
  modelName: string;
  modelCode: string;
  matchedProductId: string | null;
}

export function emptyProductFieldsValue(): ProductFieldsValue {
  return { categoryId: "", subCategoryId: "", modelName: "", modelCode: "", matchedProductId: null };
}

/** 모델코드가 있으면 productId만, 없으면 서브카테고리+모델명만 전송  */
export function toProductSelection(value: ProductFieldsValue): {
  productId: string | null;
  selectedSubCategoryId: string | null;
  selectedModelName: string | null;
} {
  if (value.matchedProductId) {
    return { productId: value.matchedProductId, selectedSubCategoryId: null, selectedModelName: null };
  }
  return {
    productId: null,
    selectedSubCategoryId: value.subCategoryId || null,
    selectedModelName: value.modelName.trim() || null,
  };
}

export function isProductFieldsValid(value: ProductFieldsValue): boolean {
  return value.matchedProductId !== null || (value.subCategoryId !== "" && value.modelName.trim() !== "");
}