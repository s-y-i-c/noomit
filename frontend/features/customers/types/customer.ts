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
