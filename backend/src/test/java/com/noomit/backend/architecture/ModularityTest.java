package com.noomit.backend.architecture;

import com.noomit.backend.NoomitBackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {
    @Test
    void verifiesModuleBoundariesAndCycles() {
        ApplicationModules.of(NoomitBackendApplication.class).verify();
    }
}
