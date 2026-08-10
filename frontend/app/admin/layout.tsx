"use client";

import type { ReactNode } from "react";
import { BarChart3, ClipboardList, Contact, PackagePlus, Users } from "lucide-react";
import { RoleLayout } from "@/features/admin/components/RoleLayout";
import type { SidebarNavItem } from "@/features/admin/components/AdminSidebar";

const NAV_ITEMS: SidebarNavItem[] = [
  { href: "/admin/members", label: "회원 관리", icon: Users },
  { href: "/admin/customers", label: "고객 관리", icon: Contact },
  { href: "/admin/repair", label: "수리 건 관리", icon: ClipboardList },
  { href: "/admin/products", label: "제품 등록", icon: PackagePlus },
  { href: "/admin/statistics", label: "통계", icon: BarChart3 },
];

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <RoleLayout
      requiredRoles={["ADMIN", "DEVELOPER"]}
      brandSub="Admin"
      navItems={NAV_ITEMS}
      storageKey="noomit-admin-sidebar-collapsed"
    >
      {children}
    </RoleLayout>
  );
}
