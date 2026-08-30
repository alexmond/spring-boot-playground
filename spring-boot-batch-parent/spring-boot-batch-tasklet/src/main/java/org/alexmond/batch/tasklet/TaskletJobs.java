package org.alexmond.batch.tasklet;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * The two step shapes, and the thing about tasklets that surprises people.
 *
 * <h2>Tasklet vs chunk</h2>
 *
 * <p>A chunk-oriented step is read/process/write with a commit every N items,
 * and Spring Batch owns the loop. A tasklet is one method; you own the loop, and
 * the framework calls it again for as long as you return
 * {@link RepeatStatus#CONTINUABLE}.
 *
 * <h2>CONTINUABLE is a new transaction each time, not a loop inside one</h2>
 *
 * <p>This is the part worth measuring rather than believing. Each re-invocation
 * runs in its <em>own</em> transaction, so a tasklet that returns CONTINUABLE is
 * how you page through a large set while committing as you go -- exactly the
 * shape a long sweep needs so that being killed halfway leaves the finished
 * pages finished.
 *
 * <p>{@link #pagingTasklet} registers an {@code afterCommit} synchronization on
 * every invocation and counts how many actually fire. A tasklet that looped
 * internally and returned FINISHED once would commit once, and would lose every
 * page on interruption.
 *
 * <p>(The obvious probe -- the current transaction's <em>name</em> -- does not
 * work: it is derived from the step, so it is identical across invocations and
 * cannot distinguish two transactions from one.)
 */
@Configuration
public class TaskletJobs {

    public static final List<Integer> CHUNK_WRITES = new CopyOnWriteArrayList<>();
    public static final AtomicInteger COMMITS = new AtomicInteger();
    public static final List<Integer> PAGED = new CopyOnWriteArrayList<>();
    public static final AtomicInteger INVOCATIONS = new AtomicInteger();

    /** The population the paging tasklet walks, 7 items at page size 2. */
    public static final List<Integer> ITEMS =
            IntStream.rangeClosed(1, 7).boxed().toList();

    private static final int PAGE = 2;

    @Bean
    public Step chunkStep(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("chunkStep", repo)
                .<Integer, Integer>chunk(3, tx)
                .reader(new ListItemReader<>(new ArrayList<>(ITEMS)))
                .processor(i -> i * 10)
                .writer(items -> CHUNK_WRITES.addAll(items.getItems()))
                .build();
    }

    @Bean
    public Step pagingTasklet(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("pagingTasklet", repo)
                .tasklet((contribution, chunkContext) -> {
                    // The transaction NAME is derived from the step and is identical on
                    // every invocation, so it cannot tell two transactions apart. An
                    // afterCommit synchronization can: it fires once per COMMITTED
                    // transaction, so the count is the number of real commits.
                    TransactionSynchronizationManager.registerSynchronization(
                            new org.springframework.transaction.support.TransactionSynchronization() {
                                @Override public void afterCommit() { COMMITS.incrementAndGet(); }
                            });
                    int from = INVOCATIONS.getAndIncrement() * PAGE;
                    if (from >= ITEMS.size()) {
                        return RepeatStatus.FINISHED;
                    }
                    PAGED.addAll(ITEMS.subList(from, Math.min(from + PAGE, ITEMS.size())));
                    return RepeatStatus.CONTINUABLE;
                }, tx)
                .build();
    }

    @Bean
    public Job chunkJob(JobRepository repo, Step chunkStep) {
        return new JobBuilder("chunkJob", repo).start(chunkStep).build();
    }

    @Bean
    public Job pagingJob(JobRepository repo, Step pagingTasklet) {
        return new JobBuilder("pagingJob", repo).start(pagingTasklet).build();
    }
}
