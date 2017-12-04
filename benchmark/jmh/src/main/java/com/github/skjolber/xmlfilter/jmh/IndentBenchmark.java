package com.github.skjolber.xmlfilter.jmh;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

import com.github.skjolber.indent.Indent;
import com.github.skjolber.indent.IndentBuilder;
import com.github.skjolber.xmlfilter.core.DefaultXmlFilter;
import com.github.skjolber.xmlfilter.core.MaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.MaxNodeLengthXmlIndentationFilter;
import com.github.skjolber.xmlfilter.core.MultiXPathMaxNodeLengthXmlIndentationFilter;
import com.github.skjolber.xmlfilter.core.MultiXPathXmlIndentationFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathXmlIndentationFilter;
import com.github.skjolber.xmlfilter.core.XmlIndentationFilter;
import com.github.skjolber.xmlfilter.jmh.filter.AaltoStaxXmlIndentationFilter;
import com.github.skjolber.xmlfilter.jmh.filter.CxfXmlIndentationFilter;
import com.github.skjolber.xmlfilter.jmh.filter.DefaultStAXXmlIndentationFilter;
import com.github.skjolber.xmlfilter.jmh.filter.TransformXmlIndentationFilter;
import com.github.skjolber.xmlfilter.jmh.filter.W3CDOMXmlIndentationFilter;
import com.github.skjolber.xmlfilter.jmh.filter.XercesSAXXmlIndentationFilter;
import com.github.skjolber.xmlfilter.jmh.utils.MapNamespaceContext;
import com.skjolberg.xmlfilter.filter.AbstractXPathFilter.FilterType;
import com.skjolberg.xmlfilter.w3c.dom.W3cDomXPathXmlFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilterFactory;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class IndentBenchmark {
		
	private BenchmarkRunner defaultXmlFilter;
	private BenchmarkRunner xmlIndentationFilter;
	private BenchmarkRunner maxNodeLengthXmlIndentationFilter;
	private BenchmarkRunner multiXPathMaxNodeLengthXmlIndentationFilter;
	private BenchmarkRunner singleXPathAnonymizeXmlIndentationFilter;
	private BenchmarkRunner singleXPathPruneXmlIndentationFilter;
	private BenchmarkRunner multiXPathXmlIndentationFilter;
	private BenchmarkRunner aaltoStaxXmlIndentationFilter;
	private BenchmarkRunner xercesSAXXmlIndentationFilter;
	private BenchmarkRunner cxfIndentationFilter;
	private BenchmarkRunner transformIndentationFilter;
	private BenchmarkRunner w3cDomIndentationFilter;
	private BenchmarkRunner w3cDomXPathIndentationFilter;
	
	@Setup
	public void init() throws Exception {
		Indent indent = new IndentBuilder().withTab().build();

		String property = System.getProperty("directory");
		
		File file = new File(property);
		
		defaultXmlFilter = new BenchmarkRunner(file, true);
		defaultXmlFilter.setXmlFilter(new DefaultXmlFilter());

		xmlIndentationFilter = new BenchmarkRunner(file, true);
		xmlIndentationFilter.setXmlFilter(new XmlIndentationFilter(false, indent));

		maxNodeLengthXmlIndentationFilter = new BenchmarkRunner(file, true);
		maxNodeLengthXmlIndentationFilter.setXmlFilter(new MaxNodeLengthXmlIndentationFilter(false, -1, -1, indent));

		singleXPathPruneXmlIndentationFilter = new BenchmarkRunner(file, true);
		singleXPathPruneXmlIndentationFilter.setXmlFilter(new SingleXPathXmlIndentationFilter(true, "/aparent/achild/*", FilterType.PRUNE, indent));

		singleXPathAnonymizeXmlIndentationFilter = new BenchmarkRunner(file, true);
		singleXPathAnonymizeXmlIndentationFilter.setXmlFilter(new SingleXPathXmlIndentationFilter(true, "/aparent/achild/*", FilterType.ANON, indent));

		multiXPathXmlIndentationFilter = new BenchmarkRunner(file, true);
		multiXPathXmlIndentationFilter.setXmlFilter(new MultiXPathXmlIndentationFilter(true, new String[]{"/aparent/achild/*"}, null, indent));

		multiXPathMaxNodeLengthXmlIndentationFilter = new BenchmarkRunner(file, true);
		multiXPathMaxNodeLengthXmlIndentationFilter.setXmlFilter(new MultiXPathMaxNodeLengthXmlIndentationFilter(true, -1, -1, new String[]{"/aparent/achild/*"}, null, indent));
		
		aaltoStaxXmlIndentationFilter = new BenchmarkRunner(file, true);
		aaltoStaxXmlIndentationFilter.setXmlFilter(new AaltoStaxXmlIndentationFilter(false, indent));
				
		xercesSAXXmlIndentationFilter = new BenchmarkRunner(file, true);
		xercesSAXXmlIndentationFilter.setXmlFilter(new XercesSAXXmlIndentationFilter(false, indent));
		
		cxfIndentationFilter = new BenchmarkRunner(file, true);
		cxfIndentationFilter.setXmlFilter(new CxfXmlIndentationFilter(false, indent));
		
		transformIndentationFilter = new BenchmarkRunner(file, true);
		transformIndentationFilter.setXmlFilter(new TransformXmlIndentationFilter(false, indent));

		w3cDomIndentationFilter = new BenchmarkRunner(file, true);
		w3cDomIndentationFilter.setXmlFilter(new W3CDOMXmlIndentationFilter(false, indent));

		XPathFilterFactory factory = new XPathFilterFactory();
		Map<String, String> namespaces = new HashMap<String, String>();
		
		String[] anon = new String[]{"/aparent/achild/*"};
		
		MapNamespaceContext context = new MapNamespaceContext(namespaces);
		XPathFilter filter = factory.getFilter(context, null, anon);
		
		w3cDomXPathIndentationFilter = new BenchmarkRunner(file, true);
		w3cDomXPathIndentationFilter.setXmlFilter(new W3cDomXPathXmlFilter(false, true, filter));

	}

	@Benchmark
    public long noop_passthrough() {
        return defaultXmlFilter.benchmark();
    }

	@Benchmark
    public long format() {
        return xmlIndentationFilter.benchmark();
    }

	@Benchmark
    public long limit() {
        return maxNodeLengthXmlIndentationFilter.benchmark();
    }
	
	@Benchmark
    public long nPruneAnonymizeLimit() {
        return multiXPathMaxNodeLengthXmlIndentationFilter.benchmark();
    }
	
	@Benchmark
    public long anonymize() {
        return singleXPathAnonymizeXmlIndentationFilter.benchmark();
    }

	@Benchmark
    public long prune() {
        return singleXPathPruneXmlIndentationFilter.benchmark();
    }

	@Benchmark
    public long nPruneAnonymize() {
        return multiXPathXmlIndentationFilter.benchmark();
    }	

	@Benchmark
    public long stax_aalto() {
        return aaltoStaxXmlIndentationFilter.benchmark();
    }	

	@Benchmark
    public long sax_xerces() {
        return xercesSAXXmlIndentationFilter.benchmark();
    }	

	@Benchmark
    public long transform_cxf() {
        return cxfIndentationFilter.benchmark();
    }	
	
	@Benchmark
    public long transform_default() {
        return transformIndentationFilter.benchmark();
    }	
	
	@Benchmark
    public long dom_w3c() {
        return w3cDomIndentationFilter.benchmark();
    }	

	@Benchmark
    public long dom_w3c_nPruneAnonymize() {
        return w3cDomXPathIndentationFilter.benchmark();
    }	

   public static void main(String[] args) throws RunnerException {
       Options opt = new OptionsBuilder()
               .include(IndentBenchmark.class.getSimpleName())
               .warmupIterations(25)
               .measurementIterations(200)
               .build();

       new Runner(opt).run();
   }
}