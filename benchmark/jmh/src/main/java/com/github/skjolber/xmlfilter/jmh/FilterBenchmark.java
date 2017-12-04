package com.github.skjolber.xmlfilter.jmh;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.codehaus.stax2.XMLOutputFactory2;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.github.skjolber.xmlfilter.core.DefaultXmlFilter;
import com.github.skjolber.xmlfilter.core.MaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.MultiXPathMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.MultiXPathXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneXmlFilter;
import com.github.skjolber.xmlfilter.jmh.utils.MapNamespaceContext;
import com.github.skjolber.xmlfilter.stax.SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter;
import com.github.skjolber.xmlfilter.stax.SingleXPathPruneMaxNodeLengthStAXXmlFilter;
import com.skjolberg.xmlfilter.w3c.dom.W3cDomXPathXmlFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilterFactory;


@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)

@Fork(1)
public class FilterBenchmark {
		
	public static final String DEFAULT_XPATH = "/aparent/achild";
	
	private BenchmarkRunner defaultXmlFilter;
	
	private BenchmarkRunner singleXPathPruneXmlFilter;
	private BenchmarkRunner singleXPathPruneMaxNodeLengthXmlFilter;
	private BenchmarkRunner singleXPathAnonymizeXmlFilter;
	private BenchmarkRunner singleXPathAnonymizeMaxNodeLengthXmlFilter;
	private BenchmarkRunner multiXPathXmlFilter;
	private BenchmarkRunner multiXPathMaxNodeLengthXmlFilter;
	
	private BenchmarkRunner maxNodeLengthXmlFilter;
	
	private BenchmarkRunner singleXPathAnonymizeMaxNodeLengthStAXXmlFilter;
	private BenchmarkRunner singleXPathPruneMaxNodeLengthStAXXmlFilter;

	
	
	private BenchmarkRunner w3cDomXPathXmlFilter;

	@Setup
	public void init() throws Exception {
		InputFactoryImpl xmlInputFactory = new InputFactoryImpl();
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);

		OutputFactoryImpl xmlOutputFactory = new OutputFactoryImpl();
		xmlOutputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
		xmlOutputFactory.setProperty(XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS, false);
		
		String property = System.getProperty("directory");
		
		String xpath = DEFAULT_XPATH;
		
		File file = new File(property);

		defaultXmlFilter = new BenchmarkRunner(file, true);
		defaultXmlFilter.setXmlFilter(new DefaultXmlFilter());

		// generic filters
		// stax
		singleXPathPruneMaxNodeLengthStAXXmlFilter = new BenchmarkRunner(file, true);
		singleXPathPruneMaxNodeLengthStAXXmlFilter.setXmlFilter(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, 127, 127, xmlInputFactory, xmlOutputFactory));

		singleXPathAnonymizeMaxNodeLengthStAXXmlFilter = new BenchmarkRunner(file, true);
		singleXPathAnonymizeMaxNodeLengthStAXXmlFilter.setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, 127, 127, xmlInputFactory, xmlOutputFactory));
		
		// xml-log-filter
		singleXPathAnonymizeXmlFilter = new BenchmarkRunner(file, true);
		singleXPathAnonymizeXmlFilter.setXmlFilter(new SingleXPathAnonymizeXmlFilter(true, xpath));

		singleXPathPruneXmlFilter = new BenchmarkRunner(file, true);
		singleXPathPruneXmlFilter.setXmlFilter(new SingleXPathPruneXmlFilter(true, xpath));

		multiXPathXmlFilter = new BenchmarkRunner(file, true);
		multiXPathXmlFilter.setXmlFilter(new MultiXPathXmlFilter(false, new String[]{xpath}, null));

		singleXPathAnonymizeMaxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		singleXPathAnonymizeMaxNodeLengthXmlFilter.setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, 127, 127));

		singleXPathPruneMaxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		singleXPathPruneMaxNodeLengthXmlFilter.setXmlFilter(new SingleXPathPruneMaxNodeLengthXmlFilter(true, xpath, 127, 127));

		multiXPathMaxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		multiXPathMaxNodeLengthXmlFilter.setXmlFilter(new MultiXPathMaxNodeLengthXmlFilter(false, 127, 127, new String[]{xpath}, null));

		maxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		maxNodeLengthXmlFilter.setXmlFilter(new MaxNodeLengthXmlFilter(false, 127, 127));
		
		// DOM
		XPathFilterFactory factory = new XPathFilterFactory();
		Map<String, String> namespaces = new HashMap<String, String>();
		
		String[] anon = new String[]{xpath};
		
		MapNamespaceContext context = new MapNamespaceContext(namespaces);
		XPathFilter filter = factory.getFilter(context, null, anon);
		
		w3cDomXPathXmlFilter = new BenchmarkRunner(file, true);
		w3cDomXPathXmlFilter.setXmlFilter(new W3cDomXPathXmlFilter(false, false, filter));
	}
	
	@Benchmark
    public long noop_passthrough() {
        return defaultXmlFilter.benchmark();
    }

	@Benchmark
    public long stax_pruneLimit() {
        return singleXPathPruneMaxNodeLengthStAXXmlFilter.benchmark();
    }

	@Benchmark
    public long stax_anonymizeLimit() {
        return singleXPathAnonymizeMaxNodeLengthStAXXmlFilter.benchmark();
    }

	@Benchmark
    public long anonymize() {
        return singleXPathAnonymizeXmlFilter.benchmark();
    }

	@Benchmark
    public long prune() {
        return singleXPathPruneXmlFilter.benchmark();
    }

	@Benchmark
    public long nPruneAnonymize() {
        return multiXPathXmlFilter.benchmark();
    }	

	@Benchmark
    public long anonymizeLimit() {
        return singleXPathAnonymizeMaxNodeLengthXmlFilter.benchmark();
    }	

	@Benchmark
    public long pruneLimit() {
        return singleXPathPruneMaxNodeLengthXmlFilter.benchmark();
    }	

	@Benchmark
    public long nPruneAnonymizeLimit() {
        return multiXPathMaxNodeLengthXmlFilter.benchmark();
    }	

	@Benchmark
	public long limit() {
		return maxNodeLengthXmlFilter.benchmark();
	}
	
	@Benchmark
    public long dom_nPruneAnonymizeLimit() {
        return w3cDomXPathXmlFilter.benchmark();
    }	

   public static void main(String[] args) throws RunnerException {
       Options opt = new OptionsBuilder()
               .include(FilterBenchmark.class.getSimpleName())
               .warmupIterations(25)
               .measurementIterations(50)
               .build();

       new Runner(opt).run();
   }
}