package com.noomit.backend.user.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Set;
import com.noomit.backend.user.UserRole;
import com.noomit.backend.user.infrastructure.security.NoomitPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class MeControllerTest {
    private final MeController controller = new MeController();

    @Test
    void returnsAccountAndDatabaseRoles() {
        NoomitPrincipal principal = new NoomitPrincipal(10L, "user@example.com", "사용자",
                "hashed-password", Set.of(UserRole.PENDING));
        var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_PENDING")));
        MeController.MeResponse response = controller.me(principal, authentication);
        assertThat(response.id()).isEqualTo("10");
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.roles()).containsExactly("ADMIN", "PENDING");
    }
}
