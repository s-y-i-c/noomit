package com.noomit.backend.config;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

// 접수번호 채번(ServiceRequestNumberService)이 REQUIRES_NEW로 메인 풀을 고갈시킬 수 있어
// 채번 전용으로 작고 독립된 풀을 둬서 메인 풀과 분리한다.

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Primary
    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    DataSource requestNumberDataSource(DataSourceProperties dataSourceProperties) {
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        dataSource.setPoolName("request-number-pool");
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }

    @Bean
    PlatformTransactionManager requestNumberTransactionManager(
            @Qualifier("requestNumberDataSource") DataSource requestNumberDataSource) {
        return new DataSourceTransactionManager(requestNumberDataSource);
    }

    @Bean
    JdbcTemplate requestNumberJdbcTemplate(@Qualifier("requestNumberDataSource") DataSource requestNumberDataSource) {
        return new JdbcTemplate(requestNumberDataSource);
    }
}