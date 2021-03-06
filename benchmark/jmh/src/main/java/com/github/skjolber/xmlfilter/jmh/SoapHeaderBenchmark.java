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
import com.github.skjolber.xmlfilter.jmh.utils.MapNamespaceContext;
import com.github.skjolber.xmlfilter.stax.SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter;
import com.github.skjolber.xmlfilter.stax.SingleXPathPruneMaxNodeLengthStAXXmlFilter;
import com.github.skjolber.xmlfilter.stax.soap.SingleXPathAnonymizeStAXSoapHeaderXmlFilter;
import com.github.skjolber.xmlfilter.stax.soap.SingleXPathPruneStAXSoapHeaderXmlFilter;
import com.skjolberg.xmlfilter.soap.SingleXPathAnonymizeSoapHeaderXmlFilter;
import com.skjolberg.xmlfilter.soap.SingleXPathPruneSoapHeaderXmlFilter;
import com.skjolberg.xmlfilter.w3c.dom.W3cDomXPathXmlFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilterFactory;

/**
 * 
 * Note that SOAP-header filters do no max node length filtering, then they would have to read the whole document.
 * 
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class SoapHeaderBenchmark {
		
	private BenchmarkRunner defaultXmlFilter;
	
	private BenchmarkRunner xpathPruneXmlFilter;
	private BenchmarkRunner xpathPruneMaxNodeLengthXmlFilter;
	private BenchmarkRunner xpathAnonymizeXmlFilter;
	private BenchmarkRunner xpathAnonymizeMaxNodeLengthXmlFilter;
	private BenchmarkRunner multiXPathXmlFilter;
	private BenchmarkRunner multiXPathMaxNodeLengthXmlFilter;
	
	private BenchmarkRunner xpathAnonymizeMaxNodeLengthStAXXmlFilter;
	private BenchmarkRunner xpathPruneMaxNodeLengthStAXXmlFilter;
	
	private BenchmarkRunner xpathAnonymizeStAXSoapHeaderXmlFilter;
	private BenchmarkRunner xpathPruneStAXSoapHeaderXmlFilter;

	private BenchmarkRunner xpathAnonymizeSoapHeaderXmlFilter;
	private BenchmarkRunner xpathPruneSoapHeaderXmlFilter;

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
		
		String xpath = "/Envelope/Header/Security/UsernameToken/Password";
		
		File file = new File(property);

		defaultXmlFilter = new BenchmarkRunner(file, true);
		defaultXmlFilter.setXmlFilter(new DefaultXmlFilter());

		// tailor made
		// stax
		xpathAnonymizeStAXSoapHeaderXmlFilter = new BenchmarkRunner(file, true);
		xpathAnonymizeStAXSoapHeaderXmlFilter.setXmlFilter(new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, xpath, 1, xmlInputFactory, xmlOutputFactory));

		xpathPruneStAXSoapHeaderXmlFilter = new BenchmarkRunner(file, true);
		xpathPruneStAXSoapHeaderXmlFilter.setXmlFilter(new SingleXPathPruneStAXSoapHeaderXmlFilter(true, xpath, 1, xmlInputFactory, xmlOutputFactory));

		// xml-log-filter
		xpathAnonymizeSoapHeaderXmlFilter = new BenchmarkRunner(file, true);
		xpathAnonymizeSoapHeaderXmlFilter.setXmlFilter(new SingleXPathAnonymizeSoapHeaderXmlFilter(true, xpath, 1));

		xpathPruneSoapHeaderXmlFilter = new BenchmarkRunner(file, true);
		xpathPruneSoapHeaderXmlFilter.setXmlFilter(new SingleXPathPruneSoapHeaderXmlFilter(true, xpath, 1));

		// generic filters
		// stax
		xpathPruneMaxNodeLengthStAXXmlFilter = new BenchmarkRunner(file, true);
		xpathPruneMaxNodeLengthStAXXmlFilter.setXmlFilter(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));

		xpathAnonymizeMaxNodeLengthStAXXmlFilter = new BenchmarkRunner(file, true);
		xpathAnonymizeMaxNodeLengthStAXXmlFilter.setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
		
		// xml-log-filter
		xpathAnonymizeXmlFilter = new BenchmarkRunner(file, true);
		xpathAnonymizeXmlFilter.setXmlFilter(new SingleXPathAnonymizeXmlFilter(true, xpath));

		xpathPruneXmlFilter = new BenchmarkRunner(file, true);
		xpathPruneXmlFilter.setXmlFilter(new SingleXPathPruneXmlFilter(true, xpath));

		multiXPathXmlFilter = new BenchmarkRunner(file, true);
		multiXPathXmlFilter.setXmlFilter(new MultiXPathXmlFilter(false, new String[]{xpath}, null));

		xpathAnonymizeMaxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		xpathAnonymizeMaxNodeLengthXmlFilter.setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, -1, -1));

		xpathPruneMaxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		xpathPruneMaxNodeLengthXmlFilter.setXmlFilter(new SingleXPathPruneMaxNodeLengthXmlFilter(true, xpath, -1, -1));

		multiXPathMaxNodeLengthXmlFilter = new BenchmarkRunner(file, true);
		multiXPathMaxNodeLengthXmlFilter.setXmlFilter(new MultiXPathMaxNodeLengthXmlFilter(false, -1, -1, new String[]{xpath}, null));

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
        return xpathPruneMaxNodeLengthStAXXmlFilter.benchmark();
    }

	@Benchmark
    public long stax_anonymizeLimit() {
        return xpathAnonymizeMaxNodeLengthStAXXmlFilter.benchmark();
    }

	@Benchmark
    public long soapheader_anonymize() {
        return xpathAnonymizeSoapHeaderXmlFilter.benchmark();
    }

	@Benchmark
    public long soapheader_prune() {
        return xpathPruneSoapHeaderXmlFilter.benchmark();
    }	

	@Benchmark
    public long soapheader_stax_anonymize() {
        return xpathAnonymizeStAXSoapHeaderXmlFilter.benchmark();
    }
	
	@Benchmark
    public long soapheader_stax_prune() {
        return xpathPruneStAXSoapHeaderXmlFilter.benchmark();
    }

	@Benchmark
    public long anonymize() {
        return xpathAnonymizeXmlFilter.benchmark();
    }

	@Benchmark
    public long prune() {
        return xpathPruneXmlFilter.benchmark();
    }

	@Benchmark
    public long nAnonymizePrune() {
        return multiXPathXmlFilter.benchmark();
    }	

	@Benchmark
    public long anonymizeLimit() {
        return xpathAnonymizeMaxNodeLengthXmlFilter.benchmark();
    }	

	@Benchmark
    public long pruneLimit() {
        return xpathPruneMaxNodeLengthXmlFilter.benchmark();
    }	

	@Benchmark
    public long nAnonymizePruneLimit() {
        return multiXPathMaxNodeLengthXmlFilter.benchmark();
    }	
	
	@Benchmark
    public long dom_nPruneAnonymizeLimit() {
        return w3cDomXPathXmlFilter.benchmark();
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