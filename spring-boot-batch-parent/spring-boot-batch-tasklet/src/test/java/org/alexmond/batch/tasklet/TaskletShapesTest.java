package org.alexmond.batch.tasklet;

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
class TaskletShapesTest {

    @Autowired JobLauncher launcher;
    @Autowired Job chunkJob;
    @Autowired Job pagingJob;

    @BeforeEach
    void reset() {
        TaskletJobs.CHUNK_WRITES.clear();
        TaskletJobs.COMMITS.set(0);
        TaskletJobs.PAGED.clear();
        TaskletJobs.INVOCATIONS.set(0);
    }

    private JobExecution run(Job job) throws Exception {
        return launcher.run(job, new JobParametersBuilder()
                .addString("run", UUID.randomUUID().toString()).toJobParameters());
    }

    @Test
    @DisplayName("a chunk step commits every N items and the framework owns the loop")
    void chunkStepWritesInBatchesOfThree() throws Exception {
        JobExecution e = run(chunkJob);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(TaskletJobs.CHUNK_WRITES)
                .as("every item, processed and written exactly once")
                .containsExactly(10, 20, 30, 40, 50, 60, 70);

        var step = e.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(7);
        assertThat(step.getWriteCount()).isEqualTo(7);
        assertThat(step.getCommitCount())
                .as("7 items at chunk 3 is 3 commits -- Batch 6 does NOT add a "
                        + "final empty one, which is what I assumed before measuring")
                .isEqualTo(3);
    }

    @Test
    @DisplayName("CONTINUABLE re-invokes the tasklet until it says FINISHED")
    void aPagingTaskletWalksThePopulation() throws Exception {
        JobExecution e = run(pagingJob);

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(TaskletJobs.PAGED)
                .as("7 items at page 2, no item seen twice, none missed")
                .containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(TaskletJobs.INVOCATIONS.get())
                .as("4 pages then one more invocation to discover it is done")
                .isEqualTo(5);
    }

    @Test
    @DisplayName("each re-invocation is its OWN transaction, which is the whole point")
    void continuableCommitsBetweenInvocations() throws Exception {
        JobExecution e = run(pagingJob);

        assertThat(TaskletJobs.COMMITS.get())
                .as("one committed transaction per invocation -- a single shared "
                        + "transaction would lose every page on interruption")
                .isEqualTo(5);

        var step = e.getStepExecutions().iterator().next();
        assertThat(step.getCommitCount())
                .as("the step agrees: a commit per invocation, not one for the step")
                .isEqualTo(5);
    }
}
