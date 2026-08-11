import { ProductEditForm } from "@/features/products/components/ProductEditForm";

export default async function AdminProductEditPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <ProductEditForm productId={id} />;
}
