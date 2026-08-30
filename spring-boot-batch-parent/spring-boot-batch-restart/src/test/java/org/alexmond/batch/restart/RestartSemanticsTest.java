package org.alexmond.batch.restart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What "continue" actually means in Spring Batch, measured rather than assumed.
 *
 * <p>The distinction these tests pin down:
 * <ul>
 *   <li>New or changed content is a <em>new JobInstance</em> -- new identifying
 *       parameters, a fresh run, no relationship to anything before it.</li>
 *   <li>An interrupted execution is <em>the same JobInstance relaunched</em>, and
 *       it resumes: completed steps are not re-executed.</li>
 * </ul>
 *
 * <p>The last test is the one that matters for a query-driven pipeline that launches every
 * job with {@code addString("runToken", UUID.randomUUID())}. That makes every
 * launch a new instance, which means the second bullet above is unreachable:
 * an interrupted job can never be continued, only started again from nothing.
 */
@SpringBootTest
class RestartSemanticsTest {

    @Autowired JobLauncher launcher;
    @Autowired Job twoStepJob;

    @BeforeEach
    void reset() {
        RestartJobs.TRACE.clear();
        RestartJobs.FAIL_SECOND.set(false);
    }

    private static JobParameters named(String v) {
        return new JobParametersBuilder().addString("scanId", v).toJobParameters();
    }

    @Test
    @DisplayName("the same identifying parameters twice is refused once completed")
    void sameParametersTwiceIsRefused() throws Exception {
        JobParameters p = named("scan-1");
        JobExecution first = launcher.run(twoStepJob, p);
        assertThat(first.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThatThrownBy(() -> launcher.run(twoStepJob, p))
                .isInstanceOf(JobInstanceAlreadyCompleteException.class);

        assertThat(RestartJobs.TRACE)
                .as("the second launch did not run anything")
                .containsExactly("first", "second");
    }

    @Test
    @DisplayName("different identifying parameters is a NEW instance, and runs everything again")
    void differentParametersIsANewInstance() throws Exception {
        JobExecution a = launcher.run(twoStepJob, named("scan-A"));
        JobExecution b = launcher.run(twoStepJob, named("scan-B"));

        assertThat(a.getJobInstance().getInstanceId())
                .as("a different scan is a different instance")
                .isNotEqualTo(b.getJobInstance().getInstanceId());
        assertThat(RestartJobs.TRACE).containsExactly("first", "second", "first", "second");
    }

    @Test
    @DisplayName("an interrupted instance, relaunched with the SAME parameters, RESUMES")
    void aFailedInstanceResumesRatherThanRestarting() throws Exception {
        JobParameters p = named("scan-interrupted");

        RestartJobs.FAIL_SECOND.set(true);
        JobExecution failed = launcher.run(twoStepJob, p);
        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(RestartJobs.TRACE).containsExactly("first", "second");

        RestartJobs.TRACE.clear();
        RestartJobs.FAIL_SECOND.set(false);
        JobExecution resumed = launcher.run(twoStepJob, p);

        assertThat(resumed.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(resumed.getJobInstance().getInstanceId())
                .as("same instance, second execution")
                .isEqualTo(failed.getJobInstance().getInstanceId());
        assertThat(RestartJobs.TRACE)
                .as("'first' had already COMPLETED, so its body must NOT run again")
                .containsExactly("second");
    }

    @Test
    @DisplayName("a random runToken makes resume unreachable -- the random-token shape")
    void aRandomTokenDefeatsResume() throws Exception {
        JobParameters p1 = new JobParametersBuilder()
                .addString("runToken", UUID.randomUUID().toString()).toJobParameters();

        RestartJobs.FAIL_SECOND.set(true);
        JobExecution failed = launcher.run(twoStepJob, p1);
        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);

        RestartJobs.TRACE.clear();
        RestartJobs.FAIL_SECOND.set(false);
        JobParameters p2 = new JobParametersBuilder()
                .addString("runToken", UUID.randomUUID().toString()).toJobParameters();
        JobExecution next = launcher.run(twoStepJob, p2);

        assertThat(next.getJobInstance().getInstanceId())
                .as("a fresh token is a different instance -- the failed one is orphaned")
                .isNotEqualTo(failed.getJobInstance().getInstanceId());
        assertThat(RestartJobs.TRACE)
                .as("'first' runs AGAIN: nothing was resumed, the work was simply redone")
                .containsExactly("first", "second");
    }
}
