/** 백엔드 UserRole enum(PENDING, COUNSELOR, ENGINEER, ADMIN, DEVELOPER)과 동일한 순서. */
export const MEMBER_ROLES = [
  "PENDING",
  "COUNSELOR",
  "ENGINEER",
  "ADMIN",
  "DEVELOPER",
] as const;
export type MemberRole = (typeof MEMBER_ROLES)[number];

/**
 * PENDING 은 서버(UserRoles 값 객체)가 모든 사용자에게 항상 강제로 포함시켜 해제할 수 없다.
 * 그래서 관리자가 실제로 토글할 수 있는 권한은 이 넷뿐이다 — UI는 이 목록만 노출한다.
 */
export const ASSIGNABLE_MEMBER_ROLES = [
  "COUNSELOR",
  "ENGINEER",
  "ADMIN",
  "DEVELOPER",
] as const satisfies readonly MemberRole[];

export type MemberStatus = "ACTIVE" | "INACTIVE";

export interface AdminMember {
  id: string;
  email: string;
  name: string;
  status: MemberStatus;
  roles: MemberRole[];
}

export interface AdminMemberPage {
  members: AdminMember[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface MemberFilters {
  query: string;
  page: number;
  size: number;
}

export interface UpdateRolesCommand {
  id: string;
  roles: MemberRole[];
}
