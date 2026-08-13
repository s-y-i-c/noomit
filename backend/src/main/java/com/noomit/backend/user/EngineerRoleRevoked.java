package com.noomit.backend.user;

/** 사용자가 ENGINEER 권한을 상실했을 때 발행 */
public record EngineerRoleRevoked(Long userId) {
}