package org.alexmond.batch.partition;

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
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PartitioningTest {

    @Autowired JobLauncher launcher;
    @Autowired Job partitionJob;

    @BeforeEach
    void reset() {
        PartitionJobs.PROCESSED_BY.clear();
        PartitionJobs.WORKER_NAMES.clear();
    }

    @Test
    @DisplayName("every item is processed exactly once, across all workers")
    void thePartitionCoversThePopulationWithoutOverlap() throws Exception {
        JobExecution e = launcher.run(partitionJob, new JobParametersBuilder()
                .addString("run", UUID.randomUUID().toString()).toJobParameters());

        assertThat(e.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(PartitionJobs.PROCESSED_BY)
                .as("no item missed and none done twice -- the map would collapse duplicates, "
                        + "so size is the real check")
                .hasSize(PartitionJobs.ITEMS);
        assertThat(PartitionJobs.PROCESSED_BY.keySet())
                .containsExactlyInAnyOrderElementsOf(
                        IntStream.range(0, PartitionJobs.ITEMS).boxed().toList());
    }

    @Test
    @DisplayName("the manager fans out to one worker step execution per partition")
    void oneWorkerStepPerPartition() throws Exception {
        JobExecution e = launcher.run(partitionJob, new JobParametersBuilder()
                .addString("run", UUID.randomUUID().toString()).toJobParameters());

        assertThat(PartitionJobs.WORKER_NAMES)
                .as("each partition runs as its own StepExecution")
                .hasSize(PartitionJobs.WORKERS);
        assertThat(e.getStepExecutions())
                .as("the manager step plus one per partition")
                .hasSize(PartitionJobs.WORKERS + 1);
    }

    @Test
    @DisplayName("stride beats contiguous when cost is clustered, and by how much")
    void strideBalancesClusteredCostAndContiguousDoesNot() {
        int n = PartitionJobs.WORKERS;

        long strideWorst = IntStream.range(0, n)
                .mapToLong(k -> PartitionJobs.strideSlice(k, n).stream()
                        .mapToLong(PartitionJobs::cost).sum())
                .max().orElseThrow();
        long contiguousWorst = IntStream.range(0, n)
                .mapToLong(k -> PartitionJobs.contiguousSlice(k, n).stream()
                        .mapToLong(PartitionJobs::cost).sum())
                .max().orElseThrow();

        long total = IntStream.range(0, PartitionJobs.ITEMS)
                .mapToLong(PartitionJobs::cost).sum();
        long ideal = total / n;

        System.out.printf("ideal/worker=%d  stride worst=%d (%.2fx)  contiguous worst=%d (%.2fx)%n",
                ideal, strideWorst, strideWorst / (double) ideal,
                contiguousWorst, contiguousWorst / (double) ideal);

        assertThat(strideWorst)
                .as("the slowest worker IS the wall clock, so this is the number that matters")
                .isLessThan(contiguousWorst);
        // Measured, not guessed: stride 1.08x ideal, contiguous 2.33x, at 60 items
        // over 4 workers with cost(id) = id*id. Stride is not perfect -- it cannot
        // be, the tail item still lands somewhere -- it is simply close, and the
        // gap between 1.08 and 2.33 is the whole argument.
        assertThat(strideWorst / (double) ideal)
                .as("stride stays near a perfect split")
                .isLessThan(1.15);
        assertThat(contiguousWorst / (double) ideal)
                .as("contiguous puts the expensive tail on one worker")
                .isGreaterThan(2.0);
        assertThat(contiguousWorst / (double) strideWorst)
                .as("the slowest contiguous worker takes over twice as long as the "
                        + "slowest stride worker -- that ratio is the wall-clock saving")
                .isGreaterThan(2.0);
    }

    @Test
    @DisplayName("both schemes cover the same population -- balance is the only difference")
    void bothSchemesAreCorrectJustNotEquallyFast() {
        int n = PartitionJobs.WORKERS;
        var byStride = IntStream.range(0, n)
                .boxed().flatMap(k -> PartitionJobs.strideSlice(k, n).stream()).sorted().toList();
        var byRange = IntStream.range(0, n)
                .boxed().flatMap(k -> PartitionJobs.contiguousSlice(k, n).stream()).sorted().toList();

        assertThat(byStride).isEqualTo(byRange);
        assertThat(byStride).hasSize(PartitionJobs.ITEMS);
    }
}
