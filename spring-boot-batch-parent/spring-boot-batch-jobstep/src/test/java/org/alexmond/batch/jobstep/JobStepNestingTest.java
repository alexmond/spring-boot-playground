package org.alexmond.batch.jobstep;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A job nested in a step: whose execution it is, whose failure it is, and which
 * parameters decide whether it can be restarted.
 */
@SpringBootTest
class JobStepNestingTest {

    @Autowired JobOperator jobOperator;
    @Autowired Job nightlyJob;
    @Autowired Job pinnedJob;
    @Autowired Job stampedJob;

    @BeforeEach
    void reset() {
        JobStepJobs.TRACE.clear();
        JobStepJobs.CHILD_EXECUTIONS.clear();
        JobStepJobs.FAIL_PARSE.set(false);
        JobStepJobs.PARENT_SAW.clear();
        JobStepJobs.PINNED_CALLS.set(0);
        JobStepJobs.STAMPED_CALLS.set(0);
    }

    private static JobParameters scan(String v) {
        return new JobParametersBuilder().addString("scan", v).toJobParameters();
    }

    private static java.util.List<String> stepNames(JobExecution e) {
        return e.getStepExecutions().stream().map(StepExecution::getStepName).toList();
    }

    @Test
    @DisplayName("the nested job runs as its own JobExecution, not as steps of the parent")
    void theChildIsAWholeSeparateExecution() throws Exception {
        JobExecution parent = jobOperator.start(nightlyJob, scan("own-execution"));

        assertThat(parent.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(JobStepJobs.TRACE)
                .as("everything ran, parent and child interleaved in flow order")
                .containsExactly("prepare", "download", "parse", "publish");

        assertThat(stepNames(parent))
                .as("the parent sees ONE step where the child job was -- not the child's steps")
                .containsExactly("prepare", "importStep", "publish");

        assertThat(JobStepJobs.CHILD_EXECUTIONS).hasSize(1);
        JobExecution child = JobStepJobs.CHILD_EXECUTIONS.get(0);
        assertThat(child.getJobInstance().getJobName()).isEqualTo("importJob");
        assertThat(child.getId())
                .as("a JobExecution of its own, with its own id")
                .isNotEqualTo(parent.getId());
        assertThat(child.getJobInstance().getInstanceId())
                .isNotEqualTo(parent.getJobInstance().getInstanceId());
        assertThat(stepNames(child)).containsExactly("download", "parse");
    }

    @Test
    @DisplayName("a nested failure fails the parent, and the parent's later steps do not run")
    void theParentSeesTheChildFailure() throws Exception {
        JobStepJobs.FAIL_PARSE.set(true);

        JobExecution parent = jobOperator.start(nightlyJob, scan("child-fails"));

        assertThat(parent.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(JobStepJobs.TRACE)
                .as("'publish' never ran -- a failed JobStep stops the parent like any other step")
                .containsExactly("prepare", "download", "parse");

        StepExecution jobStep = parent.getStepExecutions().stream()
                .filter(s -> s.getStepName().equals("importStep")).findFirst().orElseThrow();
        assertThat(jobStep.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(jobStep.getFailureExceptions())
                .as("the parent is told THAT the child failed, not why -- the child's own "
                        + "IllegalStateException stays in the child's execution")
                .singleElement()
                .satisfies(t -> assertThat(t.getMessage())
                        .contains("the delegate Job failed in JobStep"));
        assertThat(JobStepJobs.CHILD_EXECUTIONS.get(0).getAllFailureExceptions())
                .as("the real cause is recorded against the child")
                .anySatisfy(t -> assertThat(t).isInstanceOf(IllegalStateException.class));
    }

    @Test
    @DisplayName("only the ExitStatus crosses the nesting boundary -- not the child's job context")
    void nothingTheChildPromotesReachesTheParent() throws Exception {
        JobExecution parent = jobOperator.start(nightlyJob, scan("no-data-back"));

        assertThat(JobStepJobs.CHILD_EXECUTIONS.get(0).getExecutionContext()
                .getString(JobStepJobs.CHILD_NOTE))
                .as("the child promoted it correctly, into the CHILD's job context")
                .isEqualTo("17 rows");
        assertThat(parent.getExecutionContext().containsKey(JobStepJobs.CHILD_NOTE))
                .as("and the parent's job context is a different object that nothing copies into")
                .isFalse();
        assertThat(JobStepJobs.PARENT_SAW.get(JobStepJobs.CHILD_NOTE))
                .as("so the parent's next step reads nothing -- a nested job can report a "
                        + "status and nothing else")
                .isEqualTo("null");
    }

    @Test
    @DisplayName("with no extractor the child inherits every parent parameter, flags intact")
    void theDefaultExtractorCopiesTheParentParameters() throws Exception {
        JobParameters parentParams = new JobParametersBuilder()
                .addString("scan", "inherit")
                .addString("operator", "alice", false)
                .toJobParameters();

        jobOperator.start(nightlyJob, parentParams);

        JobParameters childParams = JobStepJobs.CHILD_EXECUTIONS.get(0).getJobParameters();
        assertThat(childParams.getString("scan")).isEqualTo("inherit");
        assertThat(childParams.getString("operator")).isEqualTo("alice");
        assertThat(childParams.getParameter("scan").identifying())
                .as("identifying in the parent, identifying in the child -- which is why the "
                        + "child's instance tracks the parent's")
                .isTrue();
        assertThat(childParams.getParameter("operator").identifying())
                .as("non-identifying survives the copy too, so it does not enter the child's key")
                .isFalse();
    }

    @Test
    @DisplayName("relaunching the parent resumes the CHILD as well -- restart nests")
    void restartOfTheParentResumesTheChild() throws Exception {
        JobParameters params = scan("resume-me");

        JobStepJobs.FAIL_PARSE.set(true);
        JobExecution failed = jobOperator.start(nightlyJob, params);
        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(JobStepJobs.TRACE).containsExactly("prepare", "download", "parse");

        JobStepJobs.TRACE.clear();
        JobStepJobs.FAIL_PARSE.set(false);
        JobExecution resumed = jobOperator.start(nightlyJob, params);

        assertThat(resumed.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(JobStepJobs.TRACE)
                .as("'prepare' had completed in the parent and 'download' had completed in the "
                        + "CHILD, so neither body runs again -- the resume goes two levels deep")
                .containsExactly("parse", "publish");

        assertThat(JobStepJobs.CHILD_EXECUTIONS).hasSize(2);
        assertThat(JobStepJobs.CHILD_EXECUTIONS.get(1).getJobInstance().getInstanceId())
                .as("same child instance, second child execution")
                .isEqualTo(JobStepJobs.CHILD_EXECUTIONS.get(0).getJobInstance().getInstanceId());
    }

    @Test
    @DisplayName("a fixed extractor lets the parent succeed exactly once, then fail for good")
    void aFixedChildIdentityBurnsOutAfterTheFirstRun() throws Exception {
        JobExecution first = jobOperator.start(pinnedJob, scan("pinned-1"));
        assertThat(first.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        JobStepJobs.TRACE.clear();
        JobStepJobs.CHILD_EXECUTIONS.clear();
        JobExecution second = jobOperator.start(pinnedJob, scan("pinned-2"));

        assertThat(second.getStatus())
                .as("a NEW parent instance, legitimate new work, and it cannot run")
                .isEqualTo(BatchStatus.FAILED);
        assertThat(second.getAllFailureExceptions())
                .as("the child instance for {source=catalogue} is already COMPLETED")
                .anySatisfy(t -> assertThat(t).isInstanceOf(JobInstanceAlreadyCompleteException.class));
        assertThat(JobStepJobs.TRACE)
                .as("the child never started, so nothing of it ran")
                .isEmpty();
        assertThat(JobStepJobs.CHILD_EXECUTIONS)
                .as("the second run produced no child execution at all")
                .isEmpty();
        assertThat(JobStepJobs.PINNED_CALLS.get())
                .as("the extractor was asked both times -- it is the ANSWER that is the problem")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("the extractor is consulted once per step execution, and its answer is cached "
            + "into the step context -- which is what makes the nested restart possible")
    void theExtractedParametersAreCachedAcrossRestart() throws Exception {
        JobParameters params = scan("stamped");

        JobStepJobs.FAIL_PARSE.set(true);
        JobExecution failed = jobOperator.start(stampedJob, params);
        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(JobStepJobs.STAMPED_CALLS.get()).isEqualTo(1);

        JobStepJobs.TRACE.clear();
        JobStepJobs.FAIL_PARSE.set(false);
        JobExecution resumed = jobOperator.start(stampedJob, params);

        assertThat(resumed.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(JobStepJobs.STAMPED_CALLS.get())
                .as("NOT asked again: JobStep stored the parameters in its step execution "
                        + "context on the first attempt and read them back on the restart")
                .isEqualTo(1);
        assertThat(JobStepJobs.CHILD_EXECUTIONS.get(1).getJobParameters().getString("attempt"))
                .as("so the child keeps the identity it was given the first time")
                .isEqualTo("1");
        assertThat(JobStepJobs.TRACE)
                .as("and therefore resumes rather than redoing the download")
                .containsExactly("parse");
    }
}
