"use client";

import { useSearchParams } from "next/navigation";
import { queryErrorMessage } from "@/features/store/api/queryError";
import { useAssignServiceRequestMutation, useReassignServiceRequestMutation } from "../api/serviceRequestApi";
import type { AssignServiceRequestResponse } from "../types/serviceRequest";

interface UseAssignmentSubmitResult {
  requestNumber: string | null;
  isReassign: boolean;
  actionLabel: string;
  /** 상세조회에서 배정/재배정 버튼을 눌러 들어온 경우 */
  cameFromDetail: boolean;
  isSubmitting: boolean;
  errorMessage: string | null;
  submit: (technicianId: string, slotId: string) => Promise<AssignServiceRequestResponse>;
}

/** mode/version/requestNumber를 읽어 배정/재배정 중 어느 쪽을 호출할지 결정 */
export function useAssignmentSubmit(serviceRequestId: string): UseAssignmentSubmitResult {
  const searchParams = useSearchParams();
  const requestNumber = searchParams.get("requestNumber");
  const mode = searchParams.get("mode");
  const isReassign = mode === "reassign";
  const cameFromDetail = mode !== null;
  const version = searchParams.get("version");
  const actionLabel = isReassign ? "재배정" : "배정";

  const [assignServiceRequest, assignState] = useAssignServiceRequestMutation();
  const [reassignServiceRequest, reassignState] = useReassignServiceRequestMutation();
  const submitState = isReassign ? reassignState : assignState;

  const errorMessage = submitState.isError
    ? queryErrorMessage(submitState.error, `기사를 ${actionLabel}하지 못했습니다.`)
    : null;

  const submit = (technicianId: string, slotId: string) => {
    return isReassign
      ? reassignServiceRequest({
          id: serviceRequestId,
          request: { technicianId, slotId, version: Number(version) },
        }).unwrap()
      : assignServiceRequest({
          id: serviceRequestId,
          request: { technicianId, slotId },
        }).unwrap();
  };

  return {
    requestNumber,
    isReassign,
    actionLabel,
    cameFromDetail,
    isSubmitting: submitState.isLoading,
    errorMessage,
    submit,
  };
}