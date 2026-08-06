package com.noomit.backend.user.application;

import java.util.Optional;
import com.noomit.backend.user.domain.UserAccount;

public interface UserAccountRepository {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    UserAccount insert(String email, String passwordHash, String name);
}
