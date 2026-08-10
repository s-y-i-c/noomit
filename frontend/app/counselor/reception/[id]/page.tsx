import styles from "./page.module.css";

export default async function ReceptionDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <section className={styles.page}>
      <h1>접수 상세 #{id}</h1>
      <p>업데이트 예정입니다.</p>
    </section>
  );
}