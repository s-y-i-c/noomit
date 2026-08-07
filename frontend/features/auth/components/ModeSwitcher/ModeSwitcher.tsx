"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./ModeSwitcher.module.css";

interface ModeSwitcherProps {
  roles: string[];
  compact?: boolean;
}

/** 권한 4종(대기 제외) 각각을 전용 화면에 매핑한다. */
const ROLE_DESTINATIONS: Record<string, { label: string; href: string }> = {
  COUNSELOR: { label: "상담원", href: "/counselor" },
  ENGINEER: { label: "기사", href: "/technicians" },
  ADMIN: { label: "관리자", href: "/admin" },
  DEVELOPER: { label: "개발자", href: "/developer" },
};

export function ModeSwitcher({ roles, compact = false }: ModeSwitcherProps) {
  const pathname = usePathname();

  // 이 사용자가 실제로 가진 권한만큼만 버튼을 보여준다 (최대 4개, 역할마다 화면이 따로 있음).
  const buttons = Array.from(new Set(roles))
    .map((role) => ({ role, destination: ROLE_DESTINATIONS[role] }))
    .filter((entry): entry is { role: string; destination: { label: string; href: string } } =>
      Boolean(entry.destination));

  if (buttons.length < 2) {
    return null;
  }

  return (
    <nav aria-label="화면 전환" className={styles.switcher} data-compact={compact}>
      {buttons.map(({ role, destination }) => (
        <Link
          key={role}
          href={destination.href}
          aria-current={
            pathname === destination.href || pathname.startsWith(`${destination.href}/`) ? "page" : undefined
          }
          className={styles.link}
        >
          {destination.label}
        </Link>
      ))}
    </nav>
  );
}
