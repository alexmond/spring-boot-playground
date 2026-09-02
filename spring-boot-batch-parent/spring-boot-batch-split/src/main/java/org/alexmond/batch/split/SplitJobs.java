package org.alexmond.batch.split;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Two things that only look unrelated: a {@link Flow} you can hand to more than
 * one job, and {@code split(...)}, which is the only reason the framework needs
 * flows to be first-class at all.
 *
 * <h2>Why extract a Flow</h2>
 *
 * <p>A {@code Flow} is a named, reusable piece of a job graph. {@link
 * #validationFlow} is two steps that every job in this module has to do before
 * it is allowed to publish anything, and it is declared once and referenced by
 * {@link #quickValidationJob} and {@link #publishJob}. The steps inside it stay
 * ordinary {@code Step} beans; the flow only fixes the order and the transitions
 * between them, so a change to the validation sequence lands in one place rather
 * than in every job that validates.
 *
 * <p>The limit is worth stating in the same breath: a {@code Flow} is a
 * <em>structure</em>, not an instance of anything. Reusing it across jobs is
 * fine because each job execution gets its own step executions. Using the same
 * flow object twice inside one job is not -- see the test, which pins what
 * actually happens.
 *
 * <h2>What split does, and the shape that catches people</h2>
 *
 * <p>{@code split(TaskExecutor)} runs flows concurrently and waits for all of
 * them. Two details decide how you must read every split you will ever see:
 *
 * <ol>
 *   <li><strong>The flow passed to {@code start(...)} is itself a branch.</strong>
 *       {@code start(a).split(exec).add(b)} runs {@code a} and {@code b} at the
 *       same time -- it does not run {@code a} and then branch. The count of
 *       parallel branches is one more than the number of arguments to
 *       {@code add(...)}.</li>
 *   <li><strong>The aggregate status is the worst branch status, and a failing
 *       branch does not cancel its siblings.</strong> Every branch runs to
 *       completion, the split joins, and only then does the worst status win.
 *       So a split costs the wall-clock of its slowest branch even when one of
 *       them failed in the first second.</li>
 * </ol>
 *
 * <h2>The one that cost the most to find</h2>
 *
 * <p>{@link #oddExitSplitJob} has a branch that <em>completes</em> and reports a
 * custom exit status -- the ordinary way to signal a routing decision, and
 * exactly what {@code spring-boot-batch-flow}'s {@code auditJob} does. Every
 * step in it ends COMPLETED, nothing throws, and the job ends <strong>FAILED
 * with no failure exception and an empty exit description</strong>: a job that
 * failed with nothing in it that failed.
 *
 * <p>The test also runs that same branch on its own, and it fails identically.
 * So this is not a property of {@code split} at all: <strong>a flow whose step
 * ends on a status no transition matches ends FAILED</strong>. A split merely
 * makes it much harder to see, because the parent job's status is now the only
 * evidence and it does not say which branch produced it. The remedy is a
 * transition that matches -- an exit status is only a routing signal somewhere
 * that routes on it.
 */
@Configuration
public class SplitJobs {

    /** Step bodies that executed. Order is not meaningful across parallel branches. */
    public static final List<String> TRACE = new CopyOnWriteArrayList<>();

    /** Thread each step body ran on. */
    public static final Map<String, String> THREADS = new ConcurrentHashMap<>();

    /**
     * Whether a branch found its sibling already inside the split. Both true is
     * the only outcome sequential execution cannot produce.
     */
    public static final Map<String, Boolean> MET_SIBLING = new ConcurrentHashMap<>();

    /** Reset per test; the two rendezvous steps count it down and wait on it. */
    public static final AtomicReference<CountDownLatch> RENDEZVOUS =
            new AtomicReference<>(new CountDownLatch(2));

    public static final AtomicBoolean FAIL_SETTLE_A = new AtomicBoolean(false);

    public static final String ODD_EXIT_CODE = "DISCREPANCY";

    private Step trivial(String name, JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder(name, repo)
                .tasklet((c, cc) -> {
                    TRACE.add(name);
                    THREADS.put(name, Thread.currentThread().getName());
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    /** Records whether the other branch was inside the split at the same moment. */
    private Step rendezvous(String name, JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder(name, repo)
                .tasklet((c, cc) -> {
                    TRACE.add(name);
                    THREADS.put(name, Thread.currentThread().getName());
                    CountDownLatch latch = RENDEZVOUS.get();
                    latch.countDown();
                    MET_SIBLING.put(name, latch.await(2, TimeUnit.SECONDS));
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    // ------------------------------------------------------ a flow, reused

    @Bean public Step checkSchema(JobRepository r, PlatformTransactionManager t)  { return trivial("checkSchema", r, t); }
    @Bean public Step checkLicence(JobRepository r, PlatformTransactionManager t) { return trivial("checkLicence", r, t); }
    @Bean public Step publish(JobRepository r, PlatformTransactionManager t)      { return trivial("publish", r, t); }

    /** Declared once, referenced by two jobs below. */
    @Bean
    public Flow validationFlow(Step checkSchema, Step checkLicence) {
        return new FlowBuilder<SimpleFlow>("validationFlow")
                .start(checkSchema)
                .next(checkLicence)
                .build();
    }

    @Bean
    public Job quickValidationJob(JobRepository repo, Flow validationFlow) {
        return new JobBuilder("quickValidationJob", repo)
                .start(validationFlow)
                .end()
                .build();
    }

    @Bean
    public Job publishJob(JobRepository repo, Flow validationFlow, Step publish) {
        return new JobBuilder("publishJob", repo)
                .start(validationFlow)
                .next(publish)
                .end()
                .build();
    }

    // ---------------------------------------------------------------- split

    @Bean
    public TaskExecutor splitTaskExecutor() {
        return new SimpleAsyncTaskExecutor("split-");
    }

    @Bean public Step fetchPrices(JobRepository r, PlatformTransactionManager t) { return rendezvous("fetchPrices", r, t); }
    @Bean public Step fetchStock(JobRepository r, PlatformTransactionManager t)  { return rendezvous("fetchStock", r, t); }

    @Bean
    public Flow pricesFlow(Step fetchPrices) {
        return new FlowBuilder<SimpleFlow>("pricesFlow").start(fetchPrices).build();
    }

    @Bean
    public Flow stockFlow(Step fetchStock) {
        return new FlowBuilder<SimpleFlow>("stockFlow").start(fetchStock).build();
    }

    /** One {@code add(...)} argument, TWO parallel branches. */
    @Bean
    public Job parallelJob(JobRepository repo, TaskExecutor splitTaskExecutor,
                           Flow pricesFlow, Flow stockFlow) {
        return new JobBuilder("parallelJob", repo)
                .start(pricesFlow)
                .split(splitTaskExecutor).add(stockFlow)
                .end()
                .build();
    }

    // ------------------------------------------- split with a failing branch

    @Bean
    public Step settleA(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("settleA", repo)
                .tasklet((c, cc) -> {
                    TRACE.add("settleA");
                    if (FAIL_SETTLE_A.get()) {
                        throw new IllegalStateException("deliberate failure in one branch");
                    }
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean public Step settleB(JobRepository r, PlatformTransactionManager t) { return trivial("settleB", r, t); }

    @Bean
    public Flow settleAFlow(Step settleA) {
        return new FlowBuilder<SimpleFlow>("settleAFlow").start(settleA).build();
    }

    @Bean
    public Flow settleBFlow(Step settleB) {
        return new FlowBuilder<SimpleFlow>("settleBFlow").start(settleB).build();
    }

    @Bean
    public Job failingSplitJob(JobRepository repo, TaskExecutor splitTaskExecutor,
                               Flow settleAFlow, Flow settleBFlow) {
        return new JobBuilder("failingSplitJob", repo)
                .start(settleAFlow)
                .split(splitTaskExecutor).add(settleBFlow)
                .end()
                .build();
    }

    // ---------------------------- split with a branch on a custom exit status

    /**
     * Completes normally but reports a name the flow-status ordering does not
     * recognise -- the same trick {@code spring-boot-batch-flow} uses to route
     * a successful step away from the happy path.
     */
    @Bean
    public Step reconcileOddly(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("reconcileOddly", repo)
                .tasklet((c, cc) -> {
                    TRACE.add("reconcileOddly");
                    return RepeatStatus.FINISHED;
                }, tx)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        return new ExitStatus(ODD_EXIT_CODE);
                    }
                })
                .build();
    }

    @Bean public Step settleC(JobRepository r, PlatformTransactionManager t) { return trivial("settleC", r, t); }

    @Bean
    public Flow oddFlow(Step reconcileOddly) {
        return new FlowBuilder<SimpleFlow>("oddFlow").start(reconcileOddly).build();
    }

    @Bean
    public Flow settleCFlow(Step settleC) {
        return new FlowBuilder<SimpleFlow>("settleCFlow").start(settleC).build();
    }

    @Bean
    public Job oddExitSplitJob(JobRepository repo, TaskExecutor splitTaskExecutor,
                               Flow oddFlow, Flow settleCFlow) {
        return new JobBuilder("oddExitSplitJob", repo)
                .start(oddFlow)
                .split(splitTaskExecutor).add(settleCFlow)
                .end()
                .build();
    }
}
