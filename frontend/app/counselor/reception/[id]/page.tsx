import { ServiceRequestDetail } from "@/features/reception/components/ServiceRequestDetail/ServiceRequestDetail";

export default async function ReceptionDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <ServiceRequestDetail id={id} />;
}