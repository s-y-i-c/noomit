"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LogOut, PanelLeftClose, PanelLeftOpen, type LucideIcon } from "lucide-react";
import styles from "./AdminSidebar.module.css";

export interface SidebarNavItem {
  href: string;
  label: string;
  icon: LucideIcon;
}

interface AdminSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  onLogout: () => void;
  /** 사이드바 로고 아래 서브 라벨. 역할별 화면 이름 (예: "Admin", "상담원"). */
  brandSub: string;
  navItems: SidebarNavItem[];
}

export function AdminSidebar({
  isOpen,
  onClose,
  isCollapsed,
  onToggleCollapse,
  onLogout,
  brandSub,
  navItems,
}: AdminSidebarProps) {
  const pathname = usePathname();
  const isActive = (href: string) => pathname === href || pathname.startsWith(`${href}/`);

  return (
    <>
      {isOpen ? <div onClick={onClose} className={styles.backdrop} /> : null}
      <aside
        className={`${styles.sidebar} ${isOpen ? styles.sidebarOpen : styles.sidebarClosed}`}
        data-collapsed={isCollapsed}
      >
        <div className={styles.top}>
          <div className={styles.logoArea}>
            <div className={styles.logoGroup}>
              <div className={styles.logoBadge}>N</div>
              <span className={styles.collapsible}>
                <span className={styles.logoBrand}>Noomit</span>
                <span className={styles.logoSub}>{brandSub}</span>
              </span>
            </div>
            <button
              type="button"
              onClick={onToggleCollapse}
              aria-label={isCollapsed ? "사이드바 펼치기" : "사이드바 접기"}
              className={styles.toggleButton}
            >
              {isCollapsed ? <PanelLeftOpen className={styles.toggleIcon} /> : <PanelLeftClose className={styles.toggleIcon} />}
            </button>
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
                <span className={styles.collapsible}>{label}</span>
              </Link>
            ))}
          </nav>
        </div>
        <div className={styles.bottom}>
          <button onClick={onLogout} className={styles.logoutButton}>
            <LogOut className={styles.icon} />
            <span className={styles.collapsible}>로그아웃</span>
          </button>
        </div>
      </aside>
    </>
  );
}
