package com.noomit.backend.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import com.noomit.backend.user.domain.UserAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {
    @Mock UserAccountRepository userAccountRepository;
    @Mock UserRoleRepository userRoleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks SignupService service;

    @Test
    void createsNewUserAndAssignsDefaultRole() {
        SignupCommand command = new SignupCommand("user@example.com", "plain-password", "사용자");
        UserAccount created = new UserAccount(10L, "user@example.com", "사용자", UserAccount.Status.ACTIVE);
        when(userAccountRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userAccountRepository.insert("user@example.com", "hashed-password", "사용자")).thenReturn(created);

        UserAccount result = service.signup(command);

        verify(userRoleRepository).assignDefaultRole(10L);
        assertThat(result).isEqualTo(created);
    }

    @Test
    void rejectsDuplicateEmail() {
        SignupCommand command = new SignupCommand("dup@example.com", "plain-password", "사용자");
        when(userAccountRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.signup(command))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        verify(userAccountRepository, never()).insert(any(), any(), any());
        verify(userRoleRepository, never()).assignDefaultRole(anyLong());
    }
}
