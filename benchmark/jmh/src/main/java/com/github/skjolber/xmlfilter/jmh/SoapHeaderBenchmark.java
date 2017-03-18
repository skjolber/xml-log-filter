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
import com.github.skjolber.xmlfilter.core.MultiXPathMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.MultiXPathXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneXmlFilter;
import com.github.skjolber.xmlfilter.jmh.filter.AaltoStaxXmlIndentationFilter;
import com.github.skjolber.xmlfilter.jmh.utils.MapNamespaceContext;
import com.github.skjolber.xmlfilter.stax.SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter;
import com.github.skjolber.xmlfilter.stax.SingleXPathPruneMaxNodeLengthStAXXmlFilter;
import com.github.skjolber.xmlfilter.stax.soap.SingleXPathAnonymizeStAXSoapHeaderXmlFilter;
import com.github.skjolber.xmlfilter.stax.soap.SingleXPathPruneStAXSoapHeaderXmlFilter;
import com.skjolberg.xmlfilter.soap.SingleXPathAnonymizeSoapHeaderXmlFilter;
import com.skjolberg.xmlfilter.soap.SingleXPathPruneSoapHeaderXmlFilter;
import com.skjolberg.xmlfilter.w3c.dom.W3cDomXPathXmlIndentationFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilterFactory;

/**
 * 
 * Note that SOAP-header filters do no max node length filtering, then they would have to read the whole document.
 * 
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime )
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class SoapHeaderBenchmark {
		
	private BenchmarkRunner defaultXmlFilter;
	
	private BenchmarkRunner singleXPathPruneXmlFilter;
	private BenchmarkRunner singleXPathPruneMaxNodeLengthXmlFilter;
	private BenchmarkRunner singleXPathAnonymizeXmlFilter;
	private BenchmarkRunner singleXPathAnonymizeMaxNodeLengthXmlFilter;
	private BenchmarkRunner multiXPathXmlFilter;
	private BenchmarkRunner multiXPathMaxNodeLengthXmlFilter;
	
	private BenchmarkRunner singleXPathAnonymizeMaxNodeLengthStAXXmlFilter;
	private BenchmarkRunner singleXPathPruneMaxNodeLengthStAXXmlFilter;
	
	private BenchmarkRunner singleXPathAnonymizeStAXSoapHeaderXmlFilter;
	private BenchmarkRunner singleXPathPruneStAXSoapHeaderXmlFilter;

	private BenchmarkRunner singleXPathAnonymizeSoapHeaderXmlFilter;
	private BenchmarkRunner singleXPathPruneSoapHeaderXmlFilter;

	private BenchmarkRunner w3cDomXPathXmlIndentationFilter;

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
		
		String xpath = "/Envelope/Header/Security/UsernameToken/Password";
		
		File file = new File(property);

		defaultXmlFilter = new BenchmarkRunner(file, true);
		defaultXmlFilter.setXmlFilter(new DefaultXmlFilter());

		// tailor made
		// stax
		singleXPathAnonymizeStAXSoapHeaderXmlFilter = new BenchmarkRunner(file, true);
		singleXPathAnonymizeStAXSoapHeaderXmlFilter.setXmlFilter(new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, xpath, 1, xmlInputFactory, xmlOutputFactory));

		singleXPathPruneStAXSoapHeaderXmlFilter = new BenchmarkRunner(file, true);
		singleXPathPruneStAXSoapHeaderXmlFilter.setXmlFilter(new SingleXPathPruneStAXSoapHeaderXmlFilter(true, xpath, 1, xmlInputFactory, xmlOutputFactory));

		// xml-log-filter
		singleXPathAnonymizeSoapHeaderXmlFilter = new BenchmarkRunner(file, true);
		singleXPathAnonymizeSoapHeaderXmlFilter.setXmlFilter(new SingleXPathAnonymizeSoapHeaderXmlFilter(true, xpath, 1));

		singleXPathPruneSoapHeaderXmlFilter = new BenchmarkRunner(file, true);
		singleXPathPruneSoapHeaderXmlFilter.setXmlFilter(new SingleXPathPruneSoapHeaderXmlFilter(true, xpath, 1));

		// generic filters
		// stax
		singleXPathPruneMaxNodeLengthStAXXmlFilter = new BenchmarkRunner(file, true);
		singleXPathPruneMaxNodeLengthStAXXmlFilter.setXmlFilter(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));

		singleXPathAnonymizeMaxNodeLengthStAXXmlFilter = new BenchmarkRunner(file, true);
		singleXPathAnonymizeMaxNodeLengthStAXXmlFilter.setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
		
		// xml-log-filter
		singleXPathAnonymizeXmlFilter = new BenchmarkRunner(file, true);
		singleXPathAnonymizeXmlFilter.setXmlFilter(new SingleXPathAnonymizeXmlFilter(true, xpath));

		singleXPathPruneXmlFilter = new BenchmarkRunner(file, true);
		singleXPathPruneXmlFilter.setXmlFilter(new SingleXPathPruneXmlFilter(true, xpath));

		multiXPathXmlFilter = new BenchmarkRunner(file, true);
		multiXPathXmlFilter.setXmlFilter(new MultiXPathXmlFilter(false, new String[]{xpath}, null));

		singleXPathAnonymizeMaxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		singleXPathAnonymizeMaxNodeLengthXmlFilter.setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, -1, -1));

		singleXPathPruneMaxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		singleXPathPruneMaxNodeLengthXmlFilter.setXmlFilter(new SingleXPathPruneMaxNodeLengthXmlFilter(true, xpath, -1, -1));

		multiXPathMaxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		multiXPathMaxNodeLengthXmlFilter.setXmlFilter(new MultiXPathMaxNodeLengthXmlFilter(false, -1, -1, new String[]{xpath}, null));

		// DOM
		XPathFilterFactory factory = new XPathFilterFactory();
		Map<String, String> namespaces = new HashMap<String, String>();
		
		String[] anon = new String[]{xpath};
		
		MapNamespaceContext context = new MapNamespaceContext(namespaces);
		XPathFilter filter = factory.getFilter(context, null, anon);
		
		w3cDomXPathXmlIndentationFilter = new BenchmarkRunner(file, true);
		w3cDomXPathXmlIndentationFilter.setXmlFilter(new W3cDomXPathXmlIndentationFilter(false, false, filter));
	}
	
	@Benchmark
    public long filter_noop_passthrough() {
        return defaultXmlFilter.benchmark();
    }

	@Benchmark
    public long filter_stax_singleXPathPruneMaxNodeLengthStAXXmlFilter() {
        return singleXPathPruneMaxNodeLengthStAXXmlFilter.benchmark();
    }

	@Benchmark
    public long filter_stax_singleXPathAnonymizeMaxNodeLengthStAXXmlFilter() {
        return singleXPathAnonymizeMaxNodeLengthStAXXmlFilter.benchmark();
    }

	@Benchmark
    public long filter_soapheader_singleXPathAnonymizeSoapHeaderXmlFilter() {
        return singleXPathAnonymizeSoapHeaderXmlFilter.benchmark();
    }

	@Benchmark
    public long filter_soapheader_stax_singleXPathAnonymizeStAXSoapHeaderXmlFilter() {
        return singleXPathAnonymizeStAXSoapHeaderXmlFilter.benchmark();
    }
	
	@Benchmark
    public long filter_soapheader_stax_singleXPathPruneStAXSoapHeaderXmlFilter() {
        return singleXPathPruneStAXSoapHeaderXmlFilter.benchmark();
    }

	@Benchmark
    public long filter_singleXPathXmlFilter() {
        return singleXPathAnonymizeXmlFilter.benchmark();
    }

	@Benchmark
    public long filter_singleXPathPruneXmlFilter() {
        return singleXPathPruneXmlFilter.benchmark();
    }

	@Benchmark
    public long filter_multiXPathXmlFilter() {
        return multiXPathXmlFilter.benchmark();
    }	

	@Benchmark
    public long filter_singleXPathAnonymizeMaxNodeLengthXmlFilter() {
        return singleXPathAnonymizeMaxNodeLengthXmlFilter.benchmark();
    }	

	@Benchmark
    public long filter_singleXPathPruneMaxNodeLengthXmlFilter() {
        return singleXPathPruneMaxNodeLengthXmlFilter.benchmark();
    }	

	@Benchmark
    public long filter_multiXPathMaxNodeLengthXmlFilter() {
        return multiXPathMaxNodeLengthXmlFilter.benchmark();
    }	

	@Benchmark
    public long filter_soapheader_singleXPathPruneSoapHeaderXmlFilter() {
        return multiXPathMaxNodeLengthXmlFilter.benchmark();
    }	

	
	@Benchmark
    public long filter_dom_w3cDomXPathXmlFilter() {
        return w3cDomXPathXmlIndentationFilter.benchmark();
    }	

   public static void main(String[] args) throws RunnerException {
       Options opt = new OptionsBuilder()
               .include(SoapHeaderBenchmark.class.getSimpleName())
               .warmupIterations(25)
               .measurementIterations(50)
               .build();

       new Runner(opt).run();
   }
}