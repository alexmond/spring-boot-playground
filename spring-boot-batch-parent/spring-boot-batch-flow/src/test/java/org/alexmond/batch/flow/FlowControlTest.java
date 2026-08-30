package org.alexmond.batch.flow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlowControlTest {

    @Autowired JobLauncher launcher;
    @Autowired Job auditJob;
    @Autowired Job gatedJob;

    @BeforeEach
    void reset() {
        FlowJobs.TRACE.clear();
        FlowJobs.DISCREPANCY.set(false);
        FlowJobs.POPULATION.set("SETTLED");
    }

    private JobExecution run(Job job) throws Exception {
        return launcher.run(job, new JobParametersBuilder()
                .addString("run", UUID.randomUUID().toString()).toJobParameters());
    }

    @Test
    @DisplayName("the happy path takes the * branch")
    void theWildcardBranchIsTakenWhenNothingIsWrong() throws Exception {
        JobExecution e = run(auditJob);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(FlowJobs.TRACE).containsExactly("inspect", "report");
    }

    @Test
    @DisplayName("a custom ExitStatus reroutes a step that did NOT fail")
    void aSucceedingStepCanStillRouteAwayFromTheHappyPath() throws Exception {
        FlowJobs.DISCREPANCY.set(true);

        JobExecution e = run(auditJob);

        assertThat(FlowJobs.TRACE)
                .as("routed by ExitStatus, not by failure")
                .containsExactly("inspect", "reconcile");
        assertThat(e.getStatus())
                .as("BatchStatus is COMPLETED throughout -- nothing failed")
                .isEqualTo(BatchStatus.COMPLETED);
        assertThat(e.getStepExecutions())
                .allSatisfy(s -> assertThat(s.getStatus()).isEqualTo(BatchStatus.COMPLETED));
    }

    @Test
    @DisplayName("ExitStatus and BatchStatus are different things, and only one routes")
    void exitStatusDivergesFromBatchStatus() throws Exception {
        FlowJobs.DISCREPANCY.set(true);

        JobExecution e = run(auditJob);

        var inspect = e.getStepExecutions().stream()
                .filter(s -> s.getStepName().equals("inspect")).findFirst().orElseThrow();

        assertThat(inspect.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(inspect.getExitStatus().getExitCode())
                .as("the string the flow matched on")
                .isEqualTo("DISCREPANCY");
    }

    @Test
    @DisplayName("a decider can end the job having run no step at all")
    void aShutGateRunsNothingAndLeavesNoStepExecution() throws Exception {
        FlowJobs.POPULATION.set("WAITING");

        JobExecution e = run(gatedJob);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(FlowJobs.TRACE).as("no work was done").isEmpty();
        assertThat(e.getStepExecutions())
                .as("a decider is not a step -- it leaves no StepExecution behind")
                .isEmpty();
    }

    @Test
    @DisplayName("an open gate runs the work")
    void anOpenGateProceeds() throws Exception {
        JobExecution e = run(gatedJob);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(FlowJobs.TRACE).containsExactly("work");
        assertThat(e.getStepExecutions()).hasSize(1);
    }
}
