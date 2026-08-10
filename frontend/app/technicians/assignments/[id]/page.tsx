import { AssignedRequestDetail } from "@/features/technician/components/AssignedRequestDetail";

export default async function TechnicianAssignmentDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <AssignedRequestDetail id={id} />;
}