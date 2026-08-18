"use client";

import type { ReactNode } from "react";
import { Headset } from "lucide-react";
import { RoleLayout } from "@/features/admin/components/RoleLayout";
import type { SidebarNavItem } from "@/features/admin/components/AdminSidebar";

const NAV_ITEMS: SidebarNavItem[] = [
  { href: "/counselor", label: "접수 관리", icon: Headset },
];

export default function CounselorLayout({ children }: { children: ReactNode }) {
  return (
    <RoleLayout requiredRoles={["COUNSELOR"]} navItems={NAV_ITEMS}>
      {children}
    </RoleLayout>
  );
}
