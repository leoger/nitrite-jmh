package org.dizitart.no2.v4.jmh;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.mapper.SimpleNitriteMapper;
import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.StoreModule;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.dizitart.no2.filters.FluentFilter.where;


/**
 * @author Anindya Chatterjee
 */
@Data
@State(Scope.Benchmark)
@EqualsAndHashCode(callSuper = true)
public class EntityConverterExecutionPlan extends BaseExecutionPlan<ArbitraryData> {
    private ObjectRepository<ArbitraryData> repository = null;

    @Override
    protected void setupNitrite(Database db) throws IOException {
        StoreModule storeModule = getStoreModule(db);

        SimpleNitriteMapper nitriteMapper = new SimpleNitriteMapper();
        nitriteMapper.registerEntityConverter(new ArbitraryData.Converter());

        if (storeModule != null) {
            nitrite = Nitrite.builder()
                    .loadModule(storeModule)
                    .loadModule(NitriteModule.module(nitriteMapper))
                    .openOrCreate();
            repository = nitrite.getRepository(ArbitraryData.class);
            repository.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "index1");
        } else {
            throw new NitriteIOException("failed to setup nitrite database");
        }
    }

    @Override
    protected ArbitraryData[] randomData() {
        sequence = new AtomicInteger(0);
        return IntStream.range(0, dataSetSize)
                .mapToObj(index -> randomDatum(dataSetSize))
                .toArray(ArbitraryData[]::new);
    }

    @Override
    protected void insertDataIntoNitrite(ArbitraryData[] data) {
        repository.insert(data);
    }

    @Override
    protected void insertDataIntoSQLite(ArbitraryData[] data) throws SQLException {
        sqliteConnection.setAutoCommit(false);
        val statement = sqliteConnection.prepareStatement(BenchmarkParam.INSERT_TABLE_STATEMENT);
        for (ArbitraryData datum : data) {
            statement.setInt(1, datum.id());
            statement.setString(2, datum.text());
            statement.setDouble(3, datum.number1());
            statement.setDouble(4, datum.number2());
            statement.setInt(5, datum.index1());
            statement.setBoolean(6, datum.flag1());
            statement.setBoolean(7, datum.flag2());
            statement.addBatch();
        }
        statement.executeBatch();
        sqliteConnection.setAutoCommit(true);
    }

    @Override
    public Collection<ArbitraryData> inquireNitrite(int indexValue, double value) {
        return repository.find(where("index1").eq(indexValue).and(where("number1").eq(value))).toList();
    }

    @Override
    public Collection<ArbitraryData> inquireSQLite(int indexValue, double value) throws SQLException {
        sqliteQuery.clearParameters();
        sqliteQuery.setInt(1, indexValue);
        sqliteQuery.setDouble(2, value);
        val result = sqliteQuery.executeQuery();
        val data = new ArrayList<ArbitraryData>();
        while (result.next()) {
            val datum = new ArbitraryData()
                    .id(result.getInt("id"))
                    .text(result.getString("text"))
                    .number1(result.getDouble("number1"))
                    .number2(result.getDouble("number2"))
                    .index1(result.getInt("index1"))
                    .flag1(result.getBoolean("flag1"))
                    .flag2(result.getBoolean("flag2"));
            data.add(datum);
        }
        return data;
    }

    private ArbitraryData randomDatum(int dataSetSize) {
        return new ArbitraryData()
                .id(sequence.incrementAndGet())
                .flag1(BenchmarkParam.RANDOM.nextBoolean())
                .flag2(BenchmarkParam.RANDOM.nextBoolean())
                .number1(BenchmarkParam.RANDOM.nextInt(4)/4.0) // can be 0.0, 0.25, 0.5, 0.75
                .number2(BenchmarkParam.RANDOM.nextDouble())
                .index1(BenchmarkParam.RANDOM.nextInt(dataSetSize/10))
                .text(BenchmarkParam.GENERATOR.generate(100));
    }
}
