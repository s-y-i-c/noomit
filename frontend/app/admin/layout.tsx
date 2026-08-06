"use client";

import { useEffect, useState, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { AdminHeader } from "@/features/admin/components/AdminHeader";
import { AdminSidebar } from "@/features/admin/components/AdminSidebar";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useLogoutMutation } from "@/features/auth/api/authApi";
import styles from "./layout.module.css";

export default function AdminLayout({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { status: authStatus, user, error: authError } = useAuth();
  const [logout] = useLogoutMutation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  // 백엔드 SecurityConfig가 /api/v1/admin/** 을 ADMIN·DEVELOPER 에게 허용하므로 화면 접근도 동일하게 맞춘다.
  const hasAdminRole = user?.roles.some((role) => role === "ADMIN" || role === "DEVELOPER") ?? false;

  useEffect(() => {
    if (authStatus === "unauthenticated") router.replace("/login");
    else if (authStatus === "authenticated" && !hasAdminRole) router.replace("/");
  }, [authStatus, hasAdminRole, router]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      if (window.innerWidth >= 1024) setSidebarOpen(true);
      const saved = localStorage.getItem("noomit-admin-sidebar-collapsed");
      if (saved !== null) setIsSidebarCollapsed(saved === "true");
    }, 0);
    return () => window.clearTimeout(timer);
  }, []);

  const handleToggleCollapse = () => {
    const collapsed = !isSidebarCollapsed;
    setIsSidebarCollapsed(collapsed);
    localStorage.setItem("noomit-admin-sidebar-collapsed", String(collapsed));
  };

  const handleLogout = async () => {
    await logout();
    router.replace("/login");
  };

  return (
    <div className={styles.shell}>
      {authStatus === "loading" || authStatus === "unauthenticated" ? (
        <div className={styles.state}>로그인 확인 중...</div>
      ) : authStatus === "error" ? (
        <div className={styles.state}>
          <p className={styles.stateTitle}>로그인 상태를 확인할 수 없습니다.</p>
          <p className={styles.stateDetail}>{authError}</p>
        </div>
      ) : !hasAdminRole ? (
        <div className={styles.state}>일반 사용자 화면으로 이동 중...</div>
      ) : (
        <>
          <AdminSidebar
            isOpen={sidebarOpen}
            onClose={() => setSidebarOpen(false)}
            isCollapsed={isSidebarCollapsed}
            onToggleCollapse={handleToggleCollapse}
            onLogout={handleLogout}
          />
          <div className={styles.main} data-collapsed={isSidebarCollapsed}>
            <AdminHeader
              onMenuToggle={() => setSidebarOpen(!sidebarOpen)}
              user={user}
              onLogout={handleLogout}
            />
            <main className={styles.content}>{children}</main>
          </div>
        </>
      )}
    </div>
  );
}
