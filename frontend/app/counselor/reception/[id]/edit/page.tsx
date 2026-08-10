import { ServiceRequestEditPage } from "@/features/reception/components/ServiceRequestEdit/ServiceRequestEditPage";

export default async function ReceptionEditPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <ServiceRequestEditPage id={id} />;
}