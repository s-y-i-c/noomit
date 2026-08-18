"use client";

import type { ReactNode } from "react";
import { Terminal } from "lucide-react";
import { RoleLayout } from "@/features/admin/components/RoleLayout";
import type { SidebarNavItem } from "@/features/admin/components/AdminSidebar";

const NAV_ITEMS: SidebarNavItem[] = [
  { href: "/developer", label: "개발자 도구", icon: Terminal },
];

export default function DeveloperLayout({ children }: { children: ReactNode }) {
  return (
    <RoleLayout requiredRoles={["DEVELOPER"]} navItems={NAV_ITEMS}>
      {children}
    </RoleLayout>
  );
}
