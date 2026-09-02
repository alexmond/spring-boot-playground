package org.alexmond.batch.promotion;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Handing a value from one step to a later step, and the three ways it silently
 * does not arrive.
 *
 * <h2>Two contexts, and only one of them survives the step</h2>
 *
 * <p>A step's {@code ExecutionContext} belongs to that step execution. Nothing
 * downstream can see it. The job's {@code ExecutionContext} is visible to every
 * later step. {@link ExecutionContextPromotionListener} is the bridge: after the
 * step, it copies the keys you name from the step context up into the job
 * context.
 *
 * <p>Everything below is about the fact that the bridge is opt-in per key, per
 * exit status, and -- by default -- silent when it does nothing.
 *
 * <h2>Failure mode 1: the key you forgot to name</h2>
 *
 * <p>{@link #measure} writes two keys and the listener names one. The other is
 * not an error, not a warning, and not a missing key the reader can distinguish
 * from a key whose value happens to be absent: the later step simply reads
 * {@code null}. The value is still there, in the step execution context, where
 * nothing downstream will ever look. {@code null} then flows into whatever the
 * reader does with it, and the run finishes COMPLETED.
 *
 * <h2>Failure mode 2: promotion is conditional on the exit status</h2>
 *
 * <p>The default is {@code statuses = {"COMPLETED"}}. {@link #partialMeasure}
 * completes normally and reports the custom exit status {@code PARTIAL} --
 * exactly the routing signal {@code spring-boot-batch-flow} builds on. Nothing
 * is promoted, not even the key that was named, because the listener never
 * fires. A step that succeeds and routes is the case where you most want its
 * numbers, and it is the case the default excludes.
 *
 * <h2>Failure mode 3: strict does not do what its name promises</h2>
 *
 * <p>A listener naming a key the step never wrote does nothing at all. Setting
 * {@code strict = true} makes the listener raise
 * {@code IllegalArgumentException} -- and that changes nothing anyone can act
 * on, because {@code AbstractStep} catches <em>every</em> exception a listener
 * throws from {@code afterStep} and logs it:
 *
 * <pre>Exception in afterStep callback in step %s in job %s</pre>
 *
 * <p>So the step COMPLETES, the job COMPLETES, no failure is recorded against
 * the step execution, the following step runs with nothing to read, and the only
 * trace is one ERROR line. {@link #typoJob} pins that, measured. A misspelled
 * promotion key is therefore not detectable from a job's outcome under either
 * setting -- if it matters, the reading step has to assert on what it read.
 */
@Configuration
public class PromotionJobs {

    public static final String ROW_COUNT = "rowCount";
    public static final String SCRATCH_PATH = "scratchPath";
    public static final String PARTIAL = "PARTIAL";

    /** What the reading step found in the JOB execution context. Values may be null. */
    public static final Map<String, Object> SEEN = Collections.synchronizedMap(new HashMap<>());

    /** Every key the reading step could see in the job execution context. */
    public static final Set<String> SEEN_KEYS = Collections.synchronizedSet(new TreeSet<>());

    private ExecutionContextPromotionListener promote(String... keys) {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(keys);
        return listener;
    }

    /** Writes two keys into its own step context; only one of them is promoted. */
    @Bean
    public Step measure(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("measure", repo)
                .tasklet((c, cc) -> {
                    ExecutionContext stepContext = cc.getStepContext().getStepExecution()
                            .getExecutionContext();
                    stepContext.putInt(ROW_COUNT, 42);
                    stepContext.putString(SCRATCH_PATH, "/tmp/measure-42");
                    return RepeatStatus.FINISHED;
                }, tx)
                .listener(promote(ROW_COUNT))
                .build();
    }

    /** Completes, but reports a custom exit status the default promotion ignores. */
    @Bean
    public Step partialMeasure(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("partialMeasure", repo)
                .tasklet((c, cc) -> {
                    cc.getStepContext().getStepExecution().getExecutionContext()
                            .putInt(ROW_COUNT, 7);
                    return RepeatStatus.FINISHED;
                }, tx)
                .listener(promote(ROW_COUNT))
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        return new ExitStatus(PARTIAL);
                    }
                })
                .build();
    }

    /** The same step, with the promotion told which statuses count. */
    @Bean
    public Step partialMeasureHandled(JobRepository repo, PlatformTransactionManager tx) {
        ExecutionContextPromotionListener promotion = promote(ROW_COUNT);
        promotion.setStatuses(new String[]{ExitStatus.COMPLETED.getExitCode(), PARTIAL});
        return new StepBuilder("partialMeasureHandled", repo)
                .tasklet((c, cc) -> {
                    cc.getStepContext().getStepExecution().getExecutionContext()
                            .putInt(ROW_COUNT, 7);
                    return RepeatStatus.FINISHED;
                }, tx)
                .listener(promotion)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        return new ExitStatus(PARTIAL);
                    }
                })
                .build();
    }

    /** Promotes a key nothing ever wrote, and "insists" on it. */
    @Bean
    public Step typoMeasure(JobRepository repo, PlatformTransactionManager tx) {
        ExecutionContextPromotionListener promotion = promote("rowcount");
        promotion.setStrict(true);
        return new StepBuilder("typoMeasure", repo)
                .tasklet((c, cc) -> {
                    cc.getStepContext().getStepExecution().getExecutionContext()
                            .putInt(ROW_COUNT, 42);
                    return RepeatStatus.FINISHED;
                }, tx)
                .listener(promotion)
                .build();
    }

    /** The same typo without {@code strict}: silence. */
    @Bean
    public Step lenientTypoMeasure(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("lenientTypoMeasure", repo)
                .tasklet((c, cc) -> {
                    cc.getStepContext().getStepExecution().getExecutionContext()
                            .putInt(ROW_COUNT, 42);
                    return RepeatStatus.FINISHED;
                }, tx)
                .listener(promote("rowcount"))
                .build();
    }

    /** Reads the JOB execution context, which is all a later step can see. */
    @Bean
    public Step report(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("report", repo)
                .tasklet((c, cc) -> {
                    Map<String, Object> jobContext = cc.getStepContext().getJobExecutionContext();
                    SEEN_KEYS.addAll(jobContext.keySet());
                    SEEN.put(ROW_COUNT, jobContext.get(ROW_COUNT));
                    SEEN.put(SCRATCH_PATH, jobContext.get(SCRATCH_PATH));
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Job summaryJob(JobRepository repo, Step measure, Step report) {
        return new JobBuilder("summaryJob", repo).start(measure).next(report).build();
    }

    @Bean
    public Job partialSummaryJob(JobRepository repo, Step partialMeasure, Step report) {
        return new JobBuilder("partialSummaryJob", repo)
                .start(partialMeasure)
                    .on("*").to(report)
                .end()
                .build();
    }

    @Bean
    public Job handledPartialSummaryJob(JobRepository repo, Step partialMeasureHandled, Step report) {
        return new JobBuilder("handledPartialSummaryJob", repo)
                .start(partialMeasureHandled)
                    .on("*").to(report)
                .end()
                .build();
    }

    @Bean
    public Job typoJob(JobRepository repo, Step typoMeasure, Step report) {
        return new JobBuilder("typoJob", repo).start(typoMeasure).next(report).build();
    }

    @Bean
    public Job lenientTypoJob(JobRepository repo, Step lenientTypoMeasure, Step report) {
        return new JobBuilder("lenientTypoJob", repo).start(lenientTypoMeasure).next(report).build();
    }
}
