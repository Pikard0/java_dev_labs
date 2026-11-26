package com.java.toystore.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class QuartzPersistenceConfigurationTest {

    @Test
    void liquibaseChangeSet_containsQuartzJdbcTables() throws Exception {
        String changelog = new String(new ClassPathResource("db/changelog/002-quartz-schema.sql")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertThat(changelog)
                .contains("CREATE TABLE QRTZ_JOB_DETAILS")
                .contains("CREATE TABLE QRTZ_TRIGGERS")
                .contains("CREATE TABLE QRTZ_LOCKS");
    }

    @Test
    void applicationConfiguration_usesJdbcStoreWithoutQuartzSchemaAutoInitialization() throws Exception {
        String configuration = new String(new ClassPathResource("application.yml")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertThat(configuration)
                .contains("job-store-type: jdbc")
                .contains("initialize-schema: never")
                .contains("ToyStoreScheduler");
    }
}
