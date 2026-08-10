import { Suspense } from "react";
import { TechnicianAssignForm } from "@/features/reception/components/TechnicianAssignForm";

export default async function TechnicianAssignPage({ params }: { params: Promise<{ serviceRequestId: string }> }) {
  const { serviceRequestId } = await params;
  return (
    <Suspense>
      <TechnicianAssignForm serviceRequestId={serviceRequestId} />
    </Suspense>
  );
}