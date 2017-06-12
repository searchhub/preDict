package de.cxp;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import de.cxp.spellcorrect.WordSearch;
import de.cxp.spellcorrect.predict.PreDictFactory;
import de.cxp.spellcorrect.util.TestDataProvider;

public class PreDictCEBenchmark {

	@State(Scope.Benchmark)	
	public static class Data {
		public WordSearch wordSearch = PreDictFactory.getCommunityEdition();
		public List<String> queries;
		
		@Setup
        public void up() throws IOException {
			TestDataProvider dataProvider = new TestDataProvider();
			dataProvider.populateWordSearch(wordSearch);
			queries = dataProvider.getQueries();
        }

	}
	
	@State(Scope.Thread)
    public static class Iterator {
		private int n = 0;
		String getNextQuery(List<String> queries) {
			if (n >= queries.size()) n = 0;
			return queries.get(n++);
		}
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public List<String> testSingleSearch(Data data, Iterator i) {
    	return data.wordSearch.findSimilarWords(i.getNextQuery(data.queries));
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PreDictCEBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();

    }
}
