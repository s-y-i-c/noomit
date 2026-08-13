package com.noomit.backend.user;

/** 사용자가 ENGINEER 권한을 새로 획득했을 때 발행 */
public record EngineerRoleGranted(Long userId) {
}