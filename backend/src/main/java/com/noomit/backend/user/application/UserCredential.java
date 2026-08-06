package com.noomit.backend.user.application;

import java.util.Set;
import com.noomit.backend.user.UserRole;
import com.noomit.backend.user.domain.UserAccount;

/**
 * 인증 전용 읽기 모델. 비밀번호 해시를 담고 있어 공개 {@link com.noomit.backend.user.UserDirectory}와는
 * 별도 포트({@link UserCredentialRepository})로만 노출한다.
 */
public record UserCredential(long id, String email, String passwordHash, String name,
        UserAccount.Status status, Set<UserRole> roles) {
}
