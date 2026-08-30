package org.alexmond.batch.flow;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Flow control: which step runs next, decided by the previous step's ExitStatus.
 *
 * <h2>The distinction the whole mechanism rests on</h2>
 *
 * <p>{@code BatchStatus} is the framework's own enum ({@code COMPLETED},
 * {@code FAILED}, ...) and it is what the job's outcome is recorded as.
 * {@code ExitStatus} is a <em>string</em>, it defaults to matching the
 * BatchStatus, and it is the only thing {@code on(...)} pattern-matches
 * against. A {@link StepExecutionListener} can return any ExitStatus it likes
 * without changing the BatchStatus -- which is how a step that succeeded can
 * still route somewhere other than the happy path.
 *
 * <p>{@link #auditJob} exercises exactly that: {@code inspect} always completes
 * normally, but reports {@code "DISCREPANCY"} when it finds one, and the flow
 * sends that to {@code reconcile} instead of {@code report}.
 *
 * <h2>Why a decider is not just a step that returns a status</h2>
 *
 * <p>A step reports on work it did. A {@link JobExecutionDecider} reports on the
 * <em>state of the world</em> without doing any work or leaving a StepExecution
 * behind. {@link #gatedJob} uses one for the shape a query-driven pipeline
 * actually needs: "is the upstream population settled yet?" Answering that is
 * not a unit of work, and modelling it as a step that completes having processed
 * nothing makes "the gate was shut" indistinguishable in the run history from
 * "the gate was open and there was nothing to do".
 */
@Configuration
public class FlowJobs {

    /** Steps whose body actually executed, in order. */
    public static final List<String> TRACE = new ArrayList<>();

    /** Makes {@code inspect} report DISCREPANCY while still completing normally. */
    public static final AtomicBoolean DISCREPANCY = new AtomicBoolean(false);

    /** What the decider should answer: SETTLED or WAITING. */
    public static final AtomicReference<String> POPULATION = new AtomicReference<>("SETTLED");

    private Step trivial(String name, JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder(name, repo)
                .tasklet((c, cc) -> {
                    TRACE.add(name);
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step inspect(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("inspect", repo)
                .tasklet((c, cc) -> {
                    TRACE.add("inspect");
                    return RepeatStatus.FINISHED;
                }, tx)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        // The step did not fail. It succeeded, and found something.
                        // Returning a custom ExitStatus changes only the routing.
                        return DISCREPANCY.get() ? new ExitStatus("DISCREPANCY") : null;
                    }
                })
                .build();
    }

    @Bean public Step reconcile(JobRepository repo, PlatformTransactionManager tx) { return trivial("reconcile", repo, tx); }
    @Bean public Step report(JobRepository repo, PlatformTransactionManager tx)    { return trivial("report", repo, tx); }
    @Bean public Step work(JobRepository repo, PlatformTransactionManager tx)      { return trivial("work", repo, tx); }

    @Bean
    public Job auditJob(JobRepository repo, Step inspect, Step reconcile, Step report) {
        return new JobBuilder("auditJob", repo)
                .start(inspect)
                    .on("DISCREPANCY").to(reconcile)
                .from(inspect)
                    .on("*").to(report)
                .end()
                .build();
    }

    /**
     * The gate as a decider. Note it leaves no StepExecution behind: when the
     * population has not settled the job completes having run nothing, and the
     * reason is visible as the flow's status rather than buried in a log line.
     */
    @Bean
    public JobExecutionDecider populationSettled() {
        return (jobExecution, stepExecution) -> new FlowExecutionStatus(POPULATION.get());
    }

    @Bean
    public Job gatedJob(JobRepository repo, JobExecutionDecider populationSettled, Step work) {
        return new JobBuilder("gatedJob", repo)
                .start(populationSettled)
                    .on("SETTLED").to(work)
                .from(populationSettled)
                    .on("WAITING").end()
                .end()
                .build();
    }
}
