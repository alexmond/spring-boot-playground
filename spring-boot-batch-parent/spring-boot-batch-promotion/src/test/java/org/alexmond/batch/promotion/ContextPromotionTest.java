package org.alexmond.batch.promotion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class ContextPromotionTest {

    @Autowired JobLauncher launcher;
    @Autowired Job summaryJob;
    @Autowired Job partialSummaryJob;
    @Autowired Job handledPartialSummaryJob;
    @Autowired Job typoJob;
    @Autowired Job lenientTypoJob;

    @BeforeEach
    void reset() {
        PromotionJobs.SEEN.clear();
        PromotionJobs.SEEN_KEYS.clear();
    }

    private JobExecution run(Job job) throws Exception {
        return launcher.run(job, new JobParametersBuilder()
                .addString("run", UUID.randomUUID().toString()).toJobParameters());
    }

    private static StepExecution step(JobExecution e, String name) {
        return e.getStepExecutions().stream()
                .filter(s -> s.getStepName().equals(name)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("a promoted key reaches a later step; an unpromoted one reads back null")
    void onlyTheNamedKeyCrossesTheStepBoundary() throws Exception {
        JobExecution e = run(summaryJob);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(PromotionJobs.SEEN.get(PromotionJobs.ROW_COUNT))
                .as("named in the listener, so it is in the job context")
                .isEqualTo(42);
        assertThat(PromotionJobs.SEEN.get(PromotionJobs.SCRATCH_PATH))
                .as("written by the step, not named in the listener -- the reader gets null, "
                        + "not an error, and the job still COMPLETES")
                .isNull();
        assertThat(PromotionJobs.SEEN_KEYS)
                .as("the key is not merely empty: it is not there")
                .doesNotContain(PromotionJobs.SCRATCH_PATH);
    }

    @Test
    @DisplayName("the value that did not arrive is still sitting in the writing step's own context")
    void theUnpromotedValueIsNotLostJustUnreachable() throws Exception {
        JobExecution e = run(summaryJob);

        assertThat(step(e, "measure").getExecutionContext().getString(PromotionJobs.SCRATCH_PATH))
                .as("it was written, it was stored, and nothing downstream can see it")
                .isEqualTo("/tmp/measure-42");
        assertThat(e.getExecutionContext().containsKey(PromotionJobs.SCRATCH_PATH)).isFalse();
        assertThat(e.getExecutionContext().getInt(PromotionJobs.ROW_COUNT)).isEqualTo(42);
    }

    @Test
    @DisplayName("promotion is conditional on the exit status, and the default is COMPLETED only")
    void aCustomExitStatusSuppressesPromotionEntirely() throws Exception {
        JobExecution e = run(partialSummaryJob);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(step(e, "partialMeasure").getExitStatus().getExitCode())
                .isEqualTo(PromotionJobs.PARTIAL);
        assertThat(step(e, "partialMeasure").getStatus())
                .as("the step did not fail -- it succeeded and reported something")
                .isEqualTo(BatchStatus.COMPLETED);
        assertThat(PromotionJobs.SEEN.get(PromotionJobs.ROW_COUNT))
                .as("the key WAS named, and still nothing was promoted: the listener's default "
                        + "statuses are {COMPLETED} and the exit code is PARTIAL")
                .isNull();
    }

    @Test
    @DisplayName("naming the status promotes it -- the remedy, so the default is a choice")
    void statusesCanBeWidened() throws Exception {
        JobExecution e = run(handledPartialSummaryJob);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(step(e, "partialMeasureHandled").getExitStatus().getExitCode())
                .isEqualTo(PromotionJobs.PARTIAL);
        assertThat(PromotionJobs.SEEN.get(PromotionJobs.ROW_COUNT))
                .as("same step, same exit status, one line of listener configuration different")
                .isEqualTo(7);
    }

    @Test
    @DisplayName("strict=true does not fail the step -- AbstractStep swallows every afterStep "
            + "exception, so the loud option is a log line")
    void strictOnlyBuysALogLine(CapturedOutput output) throws Exception {
        JobExecution lenient = run(lenientTypoJob);
        assertThat(lenient.getStatus())
                .as("'rowcount' was never written and nobody says anything")
                .isEqualTo(BatchStatus.COMPLETED);
        assertThat(PromotionJobs.SEEN_KEYS)
                .as("nothing of the step's own reached the job context -- 'batch.version' is "
                        + "the framework's, and is the only thing ever in there for free")
                .doesNotContain("rowcount", PromotionJobs.ROW_COUNT);

        PromotionJobs.SEEN.clear();
        PromotionJobs.SEEN_KEYS.clear();
        JobExecution strict = run(typoJob);

        assertThat(strict.getStatus())
                .as("strict DOES raise IllegalArgumentException -- and AbstractStep catches "
                        + "every exception a listener throws in afterStep and logs it, so the "
                        + "step, and the job, complete")
                .isEqualTo(BatchStatus.COMPLETED);
        assertThat(step(strict, "typoMeasure").getFailureExceptions())
                .as("nothing is recorded against the step either")
                .isEmpty();
        assertThat(strict.getStepExecutions())
                .as("and 'report' runs on regardless, with nothing to read")
                .hasSize(2);
        assertThat(PromotionJobs.SEEN.get(PromotionJobs.ROW_COUNT)).isNull();

        assertThat(output.getAll())
                .as("the ONLY difference strict=true makes is this line, in a log")
                .contains("Exception in afterStep callback")
                .contains("IllegalArgumentException");
    }
}
