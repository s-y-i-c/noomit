export type CustomerStatus = "ACTIVE" | "INACTIVE";

export interface Customer {
  id: string;
  name: string;
  phoneNumber: string;
  zipCode: string;
  address: string;
  detailAddress: string | null;
  memo: string | null;
  status: CustomerStatus;
}

export interface CustomerFilters {
  keyword: string;
  status: CustomerStatus | "";
  page: number;
  size: number;
  sort: string;
}

export interface CustomerPageData {
  customers: Customer[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** CustomerInfoForm이 다루는 편집 가능한 필드. 제출(POST /api/customers) 바디로 그대로 쓸 수 있다. */
export interface CustomerInfoFormValue {
  phoneNumber: string;
  name: string;
  zipCode: string;
  address: string;
  detailAddress: string;
  memo: string;
}
