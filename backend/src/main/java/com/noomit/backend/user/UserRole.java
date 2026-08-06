package com.noomit.backend.user;

import java.util.Locale;

/** 다른 모듈에 공개하는 사용자 권한 코드. */
public enum UserRole {
    PENDING,
    COUNSELOR,
    ENGINEER,
    ADMIN,
    DEVELOPER;

    public static UserRole from(String value) {
        return UserRole.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
