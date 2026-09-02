package org.alexmond.batch.jobstep;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.job.JobParametersExtractor;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A whole {@code Job} nested inside another job's flow, as a {@code JobStep}.
 *
 * <h2>What the nesting actually is</h2>
 *
 * <p>A {@code JobStep} is a step whose body is
 * {@code jobOperator.start(childJob, parameters)}. That is the entire mechanism,
 * and everything else follows from it:
 *
 * <ul>
 *   <li>The child gets its <strong>own JobExecution and its own JobInstance</strong>.
 *       The child's steps are not step executions of the parent -- the parent
 *       sees one step, the JobStep, and nothing about what happened inside it
 *       beyond an exit status.</li>
 *   <li>The child fails &rarr; the JobStep throws
 *       {@code UnexpectedJobExecutionException} &rarr; the parent fails. The
 *       parent cannot route around a nested failure with {@code on(...)} any
 *       differently than it would route around any other failed step.</li>
 *   <li>The child's <strong>restart identity is its parameters</strong>, and its
 *       parameters come from a {@link JobParametersExtractor}. That one object
 *       decides whether a re-run of the parent resumes the child, re-runs it, or
 *       is refused outright.</li>
 * </ul>
 *
 * <h2>The three extractors here are the three answers</h2>
 *
 * <p><b>No extractor at all</b> ({@link #nightlyJob}). The default is
 * {@code DefaultJobParametersExtractor} with {@code useAllParentParameters=true},
 * so the child is launched with a <em>copy of every parent parameter</em>,
 * identifying flags intact. The child's instance key therefore tracks the
 * parent's: a new parent instance is a new child instance, and re-launching a
 * failed parent instance lands on the same child instance and resumes it.
 * Restart nests. This is the behaviour you want and it is the behaviour you get
 * by writing nothing.
 *
 * <p><b>A fixed extractor</b> ({@link #pinnedJob}). "The child always imports
 * the catalogue" is the obvious thing to write and it is a trap: the child's
 * parameters no longer vary, so after the first parent run the child instance is
 * COMPLETED forever. The second parent run -- a different parent instance,
 * legitimately new work -- dies on
 * {@code JobInstanceAlreadyCompleteException} thrown from inside the JobStep.
 * The job succeeds once and then fails for good.
 *
 * <h2>Nothing else crosses the boundary</h2>
 *
 * <p>{@code JobStep.doExecute} sets an exit status and, if the child stopped,
 * a status. That is all it does with the child's JobExecution. A value the
 * child promotes into the <em>child's</em> job execution context is invisible
 * to the parent -- the parent's job execution context is a different object and
 * nothing copies between them. The one bit of information that crosses a
 * nesting boundary is the ExitStatus string.
 *
 * <p><b>A varying extractor</b> ({@link #stampedJob}). An extractor that stamps
 * each launch with a fresh value looks like the fix for the previous trap. What
 * it exposes instead is that {@code JobStep} <em>caches</em> the extracted
 * parameters in its own step execution context on first use, and on restart
 * reads them back rather than asking the extractor again. That cache is not an
 * optimisation -- it is what makes a nested restart possible at all, because an
 * extractor consulted a second time would hand the child a different identity
 * and silently redo completed work.
 */
@Configuration
public class JobStepJobs {

    /** Step bodies that actually executed, in order, across parent and child. */
    public static final List<String> TRACE = new CopyOnWriteArrayList<>();

    /** Makes the child's second step fail on demand. */
    public static final AtomicBoolean FAIL_PARSE = new AtomicBoolean(false);

    /** Every JobExecution the child job produced, oldest first. */
    public static final List<JobExecution> CHILD_EXECUTIONS = new CopyOnWriteArrayList<>();

    /** A value the CHILD promotes into the CHILD's job execution context. */
    public static final String CHILD_NOTE = "childNote";

    /** What the PARENT's last step could see in the PARENT's job execution context. */
    public static final Map<String, Object> PARENT_SAW = new ConcurrentHashMap<>();

    /** How many times each custom extractor was asked for parameters. */
    public static final AtomicInteger PINNED_CALLS = new AtomicInteger();
    public static final AtomicInteger STAMPED_CALLS = new AtomicInteger();

    private Step trivial(String name, JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder(name, repo)
                .tasklet((c, cc) -> {
                    TRACE.add(name);
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    // ----------------------------------------------------------------- child

    /** Promotes a value into the CHILD job's execution context, properly. */
    @Bean
    public Step download(JobRepository repo, PlatformTransactionManager tx) {
        ExecutionContextPromotionListener promotion = new ExecutionContextPromotionListener();
        promotion.setKeys(new String[]{CHILD_NOTE});
        return new StepBuilder("download", repo)
                .tasklet((c, cc) -> {
                    TRACE.add("download");
                    cc.getStepContext().getStepExecution().getExecutionContext()
                            .putString(CHILD_NOTE, "17 rows");
                    return RepeatStatus.FINISHED;
                }, tx)
                .listener(promotion)
                .build();
    }

    @Bean
    public Step parse(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("parse", repo)
                .tasklet((c, cc) -> {
                    TRACE.add("parse");
                    if (FAIL_PARSE.get()) {
                        throw new IllegalStateException("deliberate failure inside the nested job");
                    }
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    /** The job that gets nested. Nothing about it knows that it is nested. */
    @Bean
    public Job importJob(JobRepository repo, Step download, Step parse) {
        return new JobBuilder("importJob", repo)
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        CHILD_EXECUTIONS.add(jobExecution);
                    }
                })
                .start(download)
                .next(parse)
                .build();
    }

    // ---------------------------------------------------------------- parent

    @Bean
    public Step prepare(JobRepository repo, PlatformTransactionManager tx) {
        return trivial("prepare", repo, tx);
    }

    /** Reads the PARENT's job execution context, after the JobStep has run. */
    @Bean
    public Step publish(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("publish", repo)
                .tasklet((c, cc) -> {
                    TRACE.add("publish");
                    PARENT_SAW.put(CHILD_NOTE,
                            String.valueOf(cc.getStepContext().getJobExecutionContext()
                                    .get(CHILD_NOTE)));
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    /** No {@code parametersExtractor(...)}: the child inherits every parent parameter. */
    @Bean
    public Step importStep(JobRepository repo, Job importJob, JobOperator jobOperator) {
        return new StepBuilder("importStep", repo)
                .job(importJob)
                .operator(jobOperator)
                .build();
    }

    @Bean
    public Job nightlyJob(JobRepository repo, Step prepare, Step importStep, Step publish) {
        return new JobBuilder("nightlyJob", repo)
                .start(prepare).next(importStep).next(publish)
                .build();
    }

    /** Always the same child parameters, whatever the parent was launched with. */
    public static JobParametersExtractor pinnedExtractor() {
        return (job, stepExecution) -> {
            PINNED_CALLS.incrementAndGet();
            return new JobParametersBuilder().addString("source", "catalogue").toJobParameters();
        };
    }

    @Bean
    public Step pinnedImportStep(JobRepository repo, Job importJob, JobOperator jobOperator) {
        return new StepBuilder("pinnedImportStep", repo)
                .job(importJob)
                .operator(jobOperator)
                .parametersExtractor(pinnedExtractor())
                .build();
    }

    @Bean
    public Job pinnedJob(JobRepository repo, Step pinnedImportStep) {
        return new JobBuilder("pinnedJob", repo).start(pinnedImportStep).build();
    }

    /** A different child identity on every call -- if it is ever called twice. */
    public static JobParametersExtractor stampedExtractor() {
        return (job, stepExecution) -> new JobParametersBuilder()
                .addString("attempt", Integer.toString(STAMPED_CALLS.incrementAndGet()))
                .toJobParameters();
    }

    @Bean
    public Step stampedImportStep(JobRepository repo, Job importJob, JobOperator jobOperator) {
        return new StepBuilder("stampedImportStep", repo)
                .job(importJob)
                .operator(jobOperator)
                .parametersExtractor(stampedExtractor())
                .build();
    }

    @Bean
    public Job stampedJob(JobRepository repo, Step stampedImportStep) {
        return new JobBuilder("stampedJob", repo).start(stampedImportStep).build();
    }
}
