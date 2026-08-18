"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { LucideIcon } from "lucide-react";
import styles from "./AdminSidebar.module.css";

export interface SidebarNavItem {
  href: string;
  label: string;
  icon: LucideIcon;
}

interface AdminSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  navItems: SidebarNavItem[];
}

export function AdminSidebar({ isOpen, onClose, navItems }: AdminSidebarProps) {
  const pathname = usePathname();
  // href가 서로 다른 nav 항목의 prefix인 경우(예: /technicians, /technicians/products)
  // 가장 구체적인(긴) 항목 하나만 활성화한다.
  const bestMatchHref = navItems
    .map((item) => item.href)
    .filter((href) => pathname === href || pathname.startsWith(`${href}/`))
    .sort((a, b) => b.length - a.length)[0];
  const isActive = (href: string) => href === bestMatchHref;

  return (
    <>
      {isOpen ? <div onClick={onClose} className={styles.backdrop} /> : null}
      <aside className={`${styles.sidebar} ${isOpen ? styles.sidebarOpen : styles.sidebarClosed}`}>
        <div className={styles.top}>
          <div className={styles.logoArea}>
            <div className={styles.logoGroup}>
              <img src="/logo.svg" alt="Noomit" className={styles.logoBadge} />
              <span className={styles.logoBrand}>Noomit</span>
            </div>
          </div>
          <nav className={styles.nav}>
            {navItems.map(({ href, label, icon: Icon }) => (
              <Link
                key={href}
                href={href}
                className={styles.navItem}
                data-active={isActive(href)}
                onClick={onClose}
              >
                <Icon className={styles.icon} />
                <span>{label}</span>
              </Link>
            ))}
          </nav>
        </div>
      </aside>
    </>
  );
}
