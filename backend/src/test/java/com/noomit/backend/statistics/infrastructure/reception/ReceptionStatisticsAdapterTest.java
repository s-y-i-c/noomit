package com.noomit.backend.statistics.infrastructure.reception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.LocalDate;
import com.noomit.backend.reception.ReceptionStatisticsSource;
import com.noomit.backend.statistics.application.StatisticsQuery;
import com.noomit.backend.user.UserDirectory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

class ReceptionStatisticsAdapterTest {

    @Test
    void returnsEmptyDataWhenTheReceptionImplementationIsNotRegistered() {
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        ReceptionStatisticsAdapter adapter = new ReceptionStatisticsAdapter(
                beans.getBeanProvider(ReceptionStatisticsSource.class),
                mock(UserDirectory.class),
                Clock.systemUTC());

        assertThat(adapter.connected()).isFalse();
        assertThat(adapter.read(StatisticsQuery.defaults(LocalDate.of(2026, 8, 10)))).isEmpty();
    }
}
