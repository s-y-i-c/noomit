package com.noomit.backend.statistics.infrastructure.repair;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import com.noomit.backend.repair.RepairStatisticsSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

class RepairStatisticsAdapterTest {

    @Test
    void returnsEmptyDataWhenTheRepairImplementationIsNotRegistered() {
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        RepairStatisticsAdapter adapter = new RepairStatisticsAdapter(
                beans.getBeanProvider(RepairStatisticsSource.class));

        assertThat(adapter.connected()).isFalse();
        assertThat(adapter.readRepairs(List.of(1L))).isEmpty();
    }
}
