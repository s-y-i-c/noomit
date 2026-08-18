"use client";

import type { ReactNode } from "react";
import { Home } from "lucide-react";
import { RoleLayout } from "@/features/admin/components/RoleLayout";
import type { SidebarNavItem } from "@/features/admin/components/AdminSidebar";

const NAV_ITEMS: SidebarNavItem[] = [
  { href: "/", label: "홈", icon: Home },
];

// PENDING은 모든 사용자가 항상 보유하는 기본 권한이라, 사실상 로그인한 누구나 통과한다.
const REQUIRED_ROLES = ["PENDING", "COUNSELOR", "ENGINEER", "ADMIN", "DEVELOPER"];

export default function UserLayout({ children }: { children: ReactNode }) {
  return (
    <RoleLayout requiredRoles={REQUIRED_ROLES} navItems={NAV_ITEMS}>
      {children}
    </RoleLayout>
  );
}
