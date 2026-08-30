package org.alexmond.batch.partition;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

/**
 * Partitioning, and why the way you slice matters more than how many slices.
 *
 * <h2>Stride, not contiguous range</h2>
 *
 * <p>The obvious partitioner hands worker k the range {@code [k*size, (k+1)*size)}.
 * That is correct and it is usually the wrong choice, for a reason that only
 * shows up in production: <strong>ids are rarely in random order</strong>. They
 * follow insertion order, which follows directory-walk order, which groups
 * similar files together. So one contiguous range lands on the folder full of
 * 4K video and another on the folder of thumbnails, and the step takes as long
 * as its unluckiest worker.
 *
 * <p>A stride -- worker k takes every id where {@code id % n == k} -- interleaves
 * the workers through whatever ordering exists, so per-item cost variation
 * averages out across all of them instead of piling onto one.
 *
 * <p>{@link #COST} models exactly that: cost climbs steeply with the id, as it
 * would if the expensive material were clustered at one end. The test measures
 * both partitioners against it and compares the slowest worker, which is what
 * wall-clock actually is.
 */
@Configuration
public class PartitionJobs {

    public static final int ITEMS = 60;
    public static final int WORKERS = 4;

    /** Which worker processed which item. */
    public static final Map<Integer, Integer> PROCESSED_BY = new ConcurrentHashMap<>();
    public static final List<String> WORKER_NAMES = new CopyOnWriteArrayList<>();

    /** Cost of an item, clustered: the last items are far dearer than the first. */
    public static long cost(int id) {
        return id * id;
    }

    /** Worker k takes ids where id % n == k. */
    public static Partitioner stride() {
        return gridSize -> {
            Map<String, ExecutionContext> map = new HashMap<>();
            for (int k = 0; k < gridSize; k++) {
                ExecutionContext ctx = new ExecutionContext();
                ctx.putInt("mod", k);
                ctx.putInt("of", gridSize);
                map.put("stride-" + k, ctx);
            }
            return map;
        };
    }

    /** Worker k takes the contiguous block of ids [k*size, (k+1)*size). */
    public static Partitioner contiguous() {
        return gridSize -> {
            Map<String, ExecutionContext> map = new HashMap<>();
            int size = (ITEMS + gridSize - 1) / gridSize;
            for (int k = 0; k < gridSize; k++) {
                ExecutionContext ctx = new ExecutionContext();
                ctx.putInt("from", k * size);
                ctx.putInt("to", Math.min((k + 1) * size, ITEMS));
                map.put("range-" + k, ctx);
            }
            return map;
        };
    }

    /** The ids a worker owns under each scheme, so the test can weigh them. */
    public static List<Integer> strideSlice(int k, int n) {
        return IntStream.range(0, ITEMS).filter(i -> i % n == k).boxed().toList();
    }

    public static List<Integer> contiguousSlice(int k, int n) {
        int size = (ITEMS + n - 1) / n;
        return IntStream.range(k * size, Math.min((k + 1) * size, ITEMS)).boxed().toList();
    }

    @Bean
    public Step partitionWorker(JobRepository repo, PlatformTransactionManager tx) {
        return new StepBuilder("partitionWorker", repo)
                .tasklet((contribution, chunkContext) -> {
                    ExecutionContext ctx = chunkContext.getStepContext()
                            .getStepExecution().getExecutionContext();
                    String name = chunkContext.getStepContext().getStepExecution().getStepName();
                    WORKER_NAMES.add(name);
                    int mod = ctx.getInt("mod");
                    int of = ctx.getInt("of");
                    for (int id : strideSlice(mod, of)) {
                        PROCESSED_BY.put(id, mod);
                    }
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step partitionedStep(JobRepository repo, Step partitionWorker) {
        return new StepBuilder("partitionedStep", repo)
                .partitioner(partitionWorker.getName(), stride())
                .step(partitionWorker)
                .gridSize(WORKERS)
                .taskExecutor(new SimpleAsyncTaskExecutor("part-"))
                .build();
    }

    @Bean
    public Job partitionJob(JobRepository repo, Step partitionedStep) {
        return new JobBuilder("partitionJob", repo).start(partitionedStep).build();
    }
}
