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
import com.skjolberg.xmlfilter.w3c.dom.W3cDomXPathXmlIndentationFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilterFactory;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime )
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class IndentBenchmark {
		
	private BenchmarkRunner defaultXmlFilter;
	private BenchmarkRunner maxNodeLengthXmlFilter;	
	private BenchmarkRunner xmlIndentationFilter;
	private BenchmarkRunner maxNodeLengthXmlIndentationFilter;
	private BenchmarkRunner singleXPathXmlIndentationFilter;
	private BenchmarkRunner multiXPathXmlIndentationFilter;
	private BenchmarkRunner aaltoStaxXmlIndentationFilter;
	private BenchmarkRunner defaultStAXXmlIndentationFilter;
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

		maxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		maxNodeLengthXmlFilter.setXmlFilter(new MaxNodeLengthXmlFilter(false, -1, -1));

		xmlIndentationFilter = new BenchmarkRunner(file, true);
		xmlIndentationFilter.setXmlFilter(new XmlIndentationFilter(false, indent));

		maxNodeLengthXmlIndentationFilter = new BenchmarkRunner(file, true);
		maxNodeLengthXmlIndentationFilter.setXmlFilter(new MaxNodeLengthXmlIndentationFilter(false, -1, -1, indent));

		singleXPathXmlIndentationFilter = new BenchmarkRunner(file, true);
		singleXPathXmlIndentationFilter.setXmlFilter(new SingleXPathXmlIndentationFilter(true, "/aparent/achild/*", FilterType.PRUNE, indent));

		multiXPathXmlIndentationFilter = new BenchmarkRunner(file, true);
		multiXPathXmlIndentationFilter.setXmlFilter(new MultiXPathXmlIndentationFilter(true, new String[]{"/aparent/achild/*"}, null, indent));

		aaltoStaxXmlIndentationFilter = new BenchmarkRunner(file, true);
		aaltoStaxXmlIndentationFilter.setXmlFilter(new AaltoStaxXmlIndentationFilter(false, indent));
		
		defaultStAXXmlIndentationFilter = new BenchmarkRunner(file, true);
		defaultStAXXmlIndentationFilter.setXmlFilter(new DefaultStAXXmlIndentationFilter(false, indent));
		
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
		w3cDomXPathIndentationFilter.setXmlFilter(new W3cDomXPathXmlIndentationFilter(false, true, filter));

	}

	@Benchmark
    public long filter_noop_passthrough() {
        return defaultXmlFilter.benchmark();
    }

	@Benchmark
    public long filter_maxNodeLengthXmlFilter() {
        return maxNodeLengthXmlFilter.benchmark();
    }

	@Benchmark
    public long indent_filter_xmlIndentationFilter() {
        return xmlIndentationFilter.benchmark();
    }

	@Benchmark
    public long indent_filter_maxNodeLengthXmlIndentationFilter() {
        return maxNodeLengthXmlIndentationFilter.benchmark();
    }
	
	@Benchmark
    public long indent_filter_singleXPathXmlIndentationFilter() {
        return singleXPathXmlIndentationFilter.benchmark();
    }
	
	@Benchmark
    public long indent_filter_multiXPathXmlIndentationFilter() {
        return multiXPathXmlIndentationFilter.benchmark();
    }	

	@Benchmark
    public long indent_reference_aaltoStaxXmlIndentationFilter() {
        return aaltoStaxXmlIndentationFilter.benchmark();
    }	

	@Benchmark
    public long indent_reference_defaultStAXXmlIndentationFilter() {
        return defaultStAXXmlIndentationFilter.benchmark();
    }	

	@Benchmark
    public long indent_reference_xercesSAXXmlIndentationFilter() {
        return xercesSAXXmlIndentationFilter.benchmark();
    }	

	@Benchmark
    public long indent_reference_cxfXmlIndentationFilter() {
        return cxfIndentationFilter.benchmark();
    }	
	
	@Benchmark
    public long indent_reference_transformXmlIndentationFilter() {
        return transformIndentationFilter.benchmark();
    }	
	
	@Benchmark
    public long indent_dom_w3cDomXmlIndentationFilter() {
        return w3cDomIndentationFilter.benchmark();
    }	

	@Benchmark
    public long indent_reference_w3cDomXPathXmlIndentationFilter() {
        return w3cDomXPathIndentationFilter.benchmark();
    }	

   public static void main(String[] args) throws RunnerException {
       Options opt = new OptionsBuilder()
               .include(IndentBenchmark.class.getSimpleName())
               .warmupIterations(25)
               .measurementIterations(50)
               .build();

       new Runner(opt).run();
   }
}