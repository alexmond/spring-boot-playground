package org.alexmond.batch.restart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What you get when you do NOT configure a JobRepository, pinned so nobody has to
 * rediscover it during an outage.
 *
 * <p>This context deliberately keeps Boot's {@code BatchAutoConfiguration} and
 * excludes {@link PersistentBatchConfig}, i.e. it is the shape you get from
 * {@code spring-boot-starter-batch} plus a {@code DataSource} and nothing else.
 *
 * <p>The job runs. Every step executes. The job reports {@code COMPLETED}. And
 * none of it is written down: the repository is {@code ResourcelessJobRepository},
 * every execution is id 1, and not one {@code BATCH_*} table exists. Restart is
 * therefore impossible — not refused, simply absent, because there is no record
 * of a previous execution to resume or to reject.
 *
 * <p>Nothing warns you. That is the whole point of this test.
 */
@SpringBootTest
class ResourcelessDefaultTest {

    @Configuration
    @ImportAutoConfiguration(BatchAutoConfiguration.class)
    @ComponentScan(
            basePackageClasses = RestartJobs.class,
            excludeFilters = @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {PersistentBatchConfig.class, BatchRestartApplication.class}))
    static class ResourcelessContext {
        /**
         * The transaction manager that goes with a resourceless repository. Boot's
         * BatchAutoConfiguration does not contribute one, and without it the step
         * beans cannot be created at all -- which is itself worth knowing: the
         * "just add the starter" path needs a DataSource-backed transaction manager
         * from elsewhere in the context before it will even start.
         */
        @org.springframework.context.annotation.Bean
        org.springframework.transaction.PlatformTransactionManager transactionManager() {
            return new org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager();
        }
    }

    @Autowired JobRepository repository;
    @Autowired JobLauncher launcher;
    @Autowired Job twoStepJob;

    @Test
    @DisplayName("the default repository persists nothing, and says so only if you look")
    void theDefaultIsResourceless() throws Exception {
        assertThat(repository.getClass().getName())
                .as("Boot 4 ships no JDBC job-repository auto-configuration")
                .contains("Resourceless");

        RestartJobs.TRACE.clear();
        RestartJobs.FAIL_SECOND.set(false);

        JobExecution a = launcher.run(twoStepJob,
                new JobParametersBuilder().addString("scanId", "r-1").toJobParameters());
        JobExecution b = launcher.run(twoStepJob,
                new JobParametersBuilder().addString("scanId", "r-2").toJobParameters());

        assertThat(a.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(b.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(a.getJobInstanceId())
                .as("two different scans, indistinguishable: nothing was stored")
                .isEqualTo(b.getJobInstanceId())
                .isEqualTo(1L);
    }
}
