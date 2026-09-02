package org.alexmond.batch.split;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SplitAndFlowReuseTest {

    @Autowired JobLauncher launcher;
    @Autowired JobRepository jobRepository;
    @Autowired Job parallelJob;
    @Autowired Job failingSplitJob;
    @Autowired Job oddExitSplitJob;
    @Autowired Job quickValidationJob;
    @Autowired Job publishJob;
    @Autowired Flow validationFlow;
    @Autowired Flow oddFlow;
    @Autowired Step reconcileOddly;

    @BeforeEach
    void reset() {
        SplitJobs.TRACE.clear();
        SplitJobs.THREADS.clear();
        SplitJobs.MET_SIBLING.clear();
        SplitJobs.RENDEZVOUS.set(new CountDownLatch(2));
        SplitJobs.FAIL_SETTLE_A.set(false);
    }

    private JobExecution run(Job job) throws Exception {
        return launcher.run(job, new JobParametersBuilder()
                .addString("run", UUID.randomUUID().toString()).toJobParameters());
    }

    private static List<String> stepNames(JobExecution e) {
        return e.getStepExecutions().stream().map(StepExecution::getStepName).sorted().toList();
    }

    @Test
    @DisplayName("one add(...) argument means TWO parallel branches: start(...) is a branch too")
    void theStartedFlowIsItselfABranch() throws Exception {
        JobExecution e = run(parallelJob);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(stepNames(e))
                .as("start(pricesFlow).split(exec).add(stockFlow) is two branches, not one")
                .containsExactly("fetchPrices", "fetchStock");
        assertThat(SplitJobs.MET_SIBLING)
                .as("each branch found the other already inside the split -- the one outcome "
                        + "sequential execution cannot produce")
                .containsEntry("fetchPrices", true)
                .containsEntry("fetchStock", true);
    }

    @Test
    @DisplayName("branches run on the TaskExecutor's threads, not the job's")
    void branchesRunOnTheSuppliedExecutor() throws Exception {
        run(parallelJob);

        assertThat(SplitJobs.THREADS.get("fetchPrices")).startsWith("split-");
        assertThat(SplitJobs.THREADS.get("fetchStock")).startsWith("split-");
        assertThat(SplitJobs.THREADS.get("fetchPrices"))
                .as("and on different threads from each other")
                .isNotEqualTo(SplitJobs.THREADS.get("fetchStock"));
        assertThat(SplitJobs.THREADS.values())
                .as("the launching thread runs neither branch -- it waits for both")
                .doesNotContain(Thread.currentThread().getName());
    }

    @Test
    @DisplayName("a failing branch does not cancel its sibling, and the split waits for both")
    void theSplitWaitsAndAggregatesTheWorstStatus() throws Exception {
        SplitJobs.FAIL_SETTLE_A.set(true);

        JobExecution e = run(failingSplitJob);

        assertThat(e.getStatus())
                .as("the aggregate is the worst branch status")
                .isEqualTo(BatchStatus.FAILED);
        assertThat(SplitJobs.TRACE)
                .as("the healthy branch ran to completion anyway -- a split is a join, "
                        + "not a race that is abandoned on the first failure")
                .contains("settleA", "settleB");
        assertThat(e.getStepExecutions())
                .filteredOn(s -> s.getStepName().equals("settleB"))
                .singleElement()
                .satisfies(s -> assertThat(s.getStatus()).isEqualTo(BatchStatus.COMPLETED));
    }

    @Test
    @DisplayName("an exit status no transition matches ends the flow FAILED, with nothing to "
            + "point at -- and a split reports it as the whole job's status")
    void anUnmatchedExitStatusFailsTheFlowSilently() throws Exception {
        JobExecution e = run(oddExitSplitJob);

        assertThat(e.getStepExecutions())
                .as("nothing failed: both steps completed")
                .allSatisfy(s -> assertThat(s.getStatus()).isEqualTo(BatchStatus.COMPLETED));
        assertThat(e.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(e.getAllFailureExceptions())
                .as("and there is no exception to look at -- a job that failed with nothing "
                        + "in it that failed")
                .isEmpty();
        assertThat(e.getExitStatus().getExitDescription()).isEmpty();

        SplitJobs.TRACE.clear();
        Job solo = new JobBuilder("oddSoloJob", jobRepository).start(oddFlow).end().build();
        assertThat(run(solo).getStatus())
                .as("it is the FLOW, not the split: the same branch on its own fails the same "
                        + "way. A split only makes it worse by hiding which branch did it")
                .isEqualTo(BatchStatus.FAILED);

        SplitJobs.TRACE.clear();
        Flow tolerant = new FlowBuilder<SimpleFlow>("tolerantFlow")
                .start(reconcileOddly).on("*").end()
                .build();
        Job handled = new JobBuilder("tolerantJob", jobRepository).start(tolerant).end().build();
        assertThat(run(handled).getStatus())
                .as("the remedy is a transition that matches: an exit status is only a routing "
                        + "signal where something routes on it")
                .isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("one Flow bean, two jobs, and each job execution gets its own step executions")
    void theSameFlowServesMoreThanOneJob() throws Exception {
        JobExecution quick = run(quickValidationJob);
        JobExecution full = run(publishJob);

        assertThat(quick.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(full.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(stepNames(quick)).containsExactly("checkLicence", "checkSchema");
        assertThat(stepNames(full))
                .as("the shared flow, then this job's own step")
                .containsExactly("checkLicence", "checkSchema", "publish");
        assertThat(SplitJobs.TRACE)
                .as("the validation steps ran once per job, not once in total")
                .containsExactly("checkSchema", "checkLicence", "checkSchema", "checkLicence", "publish");
    }

    @Test
    @DisplayName("the same Flow object twice in ONE job is not a way to run it twice")
    void aFlowIsAStructureNotAnInstance() throws Exception {
        Job twice = new JobBuilder("twiceJob", jobRepository)
                .start(validationFlow)
                .next(validationFlow)
                .end()
                .build();

        JobExecution e = run(twice);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(SplitJobs.TRACE)
                .as("a Flow is a named node in the graph, so naming it twice adds no second "
                        + "traversal -- extract a flow to SHARE structure between jobs, never "
                        + "to repeat it inside one")
                .containsExactly("checkSchema", "checkLicence");
    }
}
