package org.alexmond.batch.jobstep;

import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * A JDBC-backed {@code JobRepository}, so that job <em>instances</em> exist.
 *
 * <h2>Why this module cannot use the default</h2>
 *
 * <p>Boot 4 ships no JDBC job-repository auto-configuration, so the default is
 * {@code ResourcelessJobRepository} -- see the {@code spring-boot-batch-restart}
 * sample, which pins that. It stores nothing, which means every lookup for a
 * previous execution comes back empty.
 *
 * <p>Everything this module is about lives in that lookup. A {@link
 * org.springframework.batch.core.step.job.JobStep} launches a nested job with a
 * set of parameters, and what happens next -- resume, refuse, or run again from
 * the start -- is decided entirely by whether the repository already holds an
 * instance for those parameters. On a resourceless repository the answer is
 * always "no", so a nested job is always a fresh run and the two sharpest edges
 * in {@link JobStepJobs} are both invisible.
 *
 * <p>Extending {@link JdbcDefaultBatchConfiguration} is enough on its own:
 * Boot's {@code BatchAutoConfiguration} is
 * {@code @ConditionalOnMissingBean(DefaultBatchConfiguration.class)}, so
 * declaring a subclass switches the resourceless default off and supplies a
 * {@code JobRepository} and a matching {@code JobOperator} -- and the JobStep
 * needs the operator, not a {@code JobLauncher}.
 *
 * <p>Creating the schema is still ours to do. {@code spring.batch.jdbc.initialize-schema}
 * is a property of an auto-configuration that no longer exists, so it does
 * nothing; the populator below is what actually creates the {@code BATCH_*}
 * tables.
 */
@Configuration
public class PersistentBatchConfig extends JdbcDefaultBatchConfiguration {

    private final DataSource dataSource;

    public PersistentBatchConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public JobRepository jobRepository() {
        DatabasePopulatorUtils.execute(new ResourceDatabasePopulator(
                new ClassPathResource("org/springframework/batch/core/schema-h2.sql")), dataSource);
        return super.jobRepository();
    }
}
