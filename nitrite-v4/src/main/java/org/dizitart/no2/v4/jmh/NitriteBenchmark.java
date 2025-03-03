package org.dizitart.no2.v4.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author Anindya Chatterjee
 */
@BenchmarkMode({
        Mode.AverageTime,
})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class NitriteBenchmark {

    @Benchmark
    @Fork(value = BenchmarkParam.FORKS, jvmArgsAppend = {
            "-Xmx8192m",
            "-Xmn6144m"})
    @Warmup(iterations = BenchmarkParam.WARMUPS, time = BenchmarkParam.MILLISECONDS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = BenchmarkParam.ITERATIONS, time = BenchmarkParam.MILLISECONDS, timeUnit = TimeUnit.MILLISECONDS)
    public void queryWithJacksonMapper(ExecutionPlan plan, Blackhole blackhole) throws Exception {
        var indexValue = BenchmarkParam.RANDOM.nextInt(plan.getDataSetSize()/10);
        var value = BenchmarkParam.RANDOM.nextInt(4)/4.0;
        Collection<ArbitraryData> results = query(plan, indexValue, value);
        blackhole.consume(results);
    }

    @Benchmark
    @Fork(value = BenchmarkParam.FORKS, jvmArgsAppend = {
            "-Xmx8192m",
            "-Xmn6144m"})
    @Warmup(iterations = BenchmarkParam.WARMUPS, time = BenchmarkParam.MILLISECONDS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = BenchmarkParam.ITERATIONS, time = BenchmarkParam.MILLISECONDS, timeUnit = TimeUnit.MILLISECONDS)
    public void queryWithMappable(EntityConverterExecutionPlan plan, Blackhole blackhole) throws Exception {
        var indexValue = BenchmarkParam.RANDOM.nextInt(plan.getDataSetSize()/2);
        var value = BenchmarkParam.RANDOM.nextInt(4)/4.0;
        Collection<ArbitraryData> results = query(plan, indexValue, value);
        blackhole.consume(results);
    }

    @Benchmark
    @Fork(value = BenchmarkParam.FORKS, jvmArgsAppend = {
            "-Xmx8192m",
            "-Xmn6144m"})
    @Warmup(iterations = BenchmarkParam.WARMUPS, time = BenchmarkParam.MILLISECONDS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = BenchmarkParam.ITERATIONS, time = BenchmarkParam.MILLISECONDS, timeUnit = TimeUnit.MILLISECONDS)
    public void noopBaseline(Blackhole blackhole) throws Exception {
        var indexValue = BenchmarkParam.RANDOM.nextInt();
        var value = BenchmarkParam.RANDOM.nextInt(4)/4.0;
        blackhole.consume(indexValue + value);
    }

    private <T> Collection<T> query(BaseExecutionPlan<T> plan, int indexValue, double value) throws SQLException {
        Collection<T> results = null;
        switch (plan.getDatabase()) {
            case NITRITE_MVSTORE_FILE:
            case NITRITE_ROCKSDB_FILE:
            case NITRITE_MVSTORE_MEMORY:
                results = plan.inquireNitrite(indexValue, value);
                break;
            case SQLITE_FILE:
            case SQLITE_MEMORY:
                results = plan.inquireSQLite(indexValue, value);
                break;
        }
        return results;
    }
}
