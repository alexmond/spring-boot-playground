package org.alexmond.batch.restart;

import org.springframework.batch.core.configuration.support.ScopeConfiguration;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * A JDBC-backed {@code JobRepository}, wired by hand.
 *
 * <h2>Why this class has to exist at all</h2>
 *
 * <p>Spring Boot 4 ships no JDBC job-repository auto-configuration. The whole of
 * {@code org.springframework.boot.batch.autoconfigure} is eleven classes and none
 * of them builds one; there is no {@code BatchJdbcAutoConfiguration}. So adding
 * {@code spring-boot-starter-batch} and a {@code DataSource} does <em>not</em>
 * give you a persistent repository.
 *
 * <p>What you get instead is Spring Batch 6's default,
 * {@code ResourcelessJobRepository}. It is not a broken state and it raises
 * nothing: jobs run, steps execute, and the job reports {@code COMPLETED}. It
 * simply stores nothing. Every execution comes back with id {@code 1}, no
 * {@code BATCH_*} table is ever created, and — the part that matters —
 * <strong>restart does not exist</strong>. There is no record of a previous
 * execution, so there is nothing to resume and nothing to refuse.
 *
 * <p>That failure is silent in the worst way: a job configured this way looks
 * healthy for as long as nothing interrupts it. {@code ResourcelessDefaultTest}
 * pins the behaviour so it is discovered here rather than after a server restart
 * in the middle of a long run.
 *
 * <p>{@code @Import(ScopeConfiguration.class)} registers the {@code step} and
 * {@code job} scopes. Without {@code @EnableBatchProcessing} nothing else does,
 * and a {@code @StepScope} bean then fails at startup with
 * "No Scope registered for scope name 'step'".
 */
@Configuration
@Import(ScopeConfiguration.class)
public class PersistentBatchConfig {

    @Bean
    public PlatformTransactionManager batchTransactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager batchTransactionManager)
            throws Exception {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("org/springframework/batch/core/schema-h2.sql"));
        DatabasePopulatorUtils.execute(populator, dataSource);

        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(batchTransactionManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SyncTaskExecutor());
        launcher.afterPropertiesSet();
        return launcher;
    }
}
