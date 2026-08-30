package org.alexmond.batch.restart;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A two-step job whose second step fails on demand, so the same JobInstance can
 * be relaunched and observed resuming.
 *
 * <p>Every step records that it ran into {@link #TRACE}. That list is the whole
 * point: it is how a restart is told apart from a re-run. Spring Batch does not
 * announce "I skipped step one" -- the only evidence is that step one's body
 * did not execute a second time.
 */
@Configuration
public class RestartJobs {

    /** Names of steps that actually executed their body, in order, across all runs. */
    public static final List<String> TRACE = new ArrayList<>();

    /** Flipped by a test to make {@code second} fail exactly once. */
    public static final AtomicBoolean FAIL_SECOND = new AtomicBoolean(false);

    @Bean
    public Step first(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("first", repo)
                .tasklet((contribution, chunkContext) -> {
                    TRACE.add("first");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step second(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("second", repo)
                .tasklet((contribution, chunkContext) -> {
                    TRACE.add("second");
                    if (FAIL_SECOND.get()) {
                        throw new IllegalStateException("deliberate failure, so the instance can be restarted");
                    }
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Job twoStepJob(JobRepository repo, Step first, Step second) {
        return new JobBuilder("twoStepJob", repo).start(first).next(second).build();
    }
}
