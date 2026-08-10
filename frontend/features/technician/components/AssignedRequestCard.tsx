"use client";

import { useRouter } from "next/navigation";
import type { MyAssignedRequest } from "../types/assignedRequest";
import { toMinutePrefix } from "./dateUtils";
import styles from "./AssignedRequestCard.module.css";

interface AssignedRequestCardProps {
  request: MyAssignedRequest;
}

export function AssignedRequestCard({ request }: AssignedRequestCardProps) {
  const router = useRouter();

  return (
    <article
      className={styles.card}
      onClick={() => router.push(`/technicians/assignments/${request.serviceRequestId}`)}
    >
      <div className={styles.timeColumn}>
        <span>{toMinutePrefix(request.startTime)}</span>
        <span>{toMinutePrefix(request.endTime)}</span>
      </div>
      <div className={styles.content}>
        <p className={styles.title}>{request.customerName} · {request.modelName}</p>
        <p className={styles.address}>{request.address}</p>
        <span className={styles.statusBadge}>
          <span className={styles.statusDot} />
          배정
        </span>
      </div>
    </article>
  );
}