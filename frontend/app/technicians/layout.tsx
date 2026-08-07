"use client";

import type { ReactNode } from "react";
import { Wrench } from "lucide-react";
import { RoleLayout } from "@/features/admin/components/RoleLayout";
import type { SidebarNavItem } from "@/features/admin/components/AdminSidebar";

const NAV_ITEMS: SidebarNavItem[] = [
  { href: "/technicians", label: "수리 현황", icon: Wrench },
];

export default function TechniciansLayout({ children }: { children: ReactNode }) {
  return (
    <RoleLayout
      requiredRoles={["ENGINEER"]}
      brandSub="기사"
      navItems={NAV_ITEMS}
      storageKey="noomit-technicians-sidebar-collapsed"
    >
      {children}
    </RoleLayout>
  );
}
