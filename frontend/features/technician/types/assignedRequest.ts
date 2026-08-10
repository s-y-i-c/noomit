export interface MyAssignedRequest {
  serviceRequestId: string;
  requestNumber: string;
  customerName: string;
  address: string;
  modelName: string;
  startTime: string;
  endTime: string;
}

export interface MyAssignedRequestDetail {
  serviceRequestId: string;
  requestNumber: string;
  customerName: string;
  customerPhone: string;
  address: string;
  detailAddress: string;
  modelName: string;
  symptom: string;
  remarks: string;
  visitDate: string;
  startTime: string;
  endTime: string;
}