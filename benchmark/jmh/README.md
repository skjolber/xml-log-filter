#JMH benchmarks

[JMH] is a modern benchmarking framework. This project implements a few test benches for the XML filters included in this GIT repository.

# Usage
Run java command line

```
java -Ddirectory=<dir> -jar target/benchmarks.jar <benchmark name>
```

e.g. 

```
java -Ddirectory=./src/test/resources/soap -jar target/benchmarks.jar SoapHeaderBench
```

# IndentBenchmark
Benchmark including all pretty-printing xml-filters from this project, compared to various (more or less) reference implementations:

 * StAX
   * Default implementation
   * [Aalto XML]
   * CXF
 * SAX using Xerces
 * Transform
 * W3C DOM 

Implementations which offer additional filter capabilities (XPath, text / CDTA node size) are configured in such a way that those capabilities do not take effect. These are

 * XmlIndentationFilter
 * MaxNodeLengthXmlIndentationFilter
 * SingleXPathXmlIndentationFilter
 * MultiXPathXmlIndentationFilter

In addition, a reference implementation, 
 
 * DefaultXmlFilter

is included. This filter simply copies from input to output and serves as a minimum threshold and sanity check. Run using command

```
java -Ddirectory=./src/test/resources/soap -jar target/benchmarks.jar IndentBenchmark
```

# SoapHeaderBenchmark
The SOAP header use-case requires the first part of the XML document to be filtered, whereas the bulk is to be skipped (kept as-is). A few reference implementations
 
 * W3C DOM with XPath filter

are compared to tailor-made filters

 * SingleXPathAnonymizeSoapHeaderXmlFilter 
 * SingleXPathPruneSoapHeaderXmlFilter 
 * SingleXPathAnonymizeStAXSoapHeaderXmlFilter
 * SingleXPathPruneStAXSoapHeaderXmlFilter
 
And a few generic filters

 * SingleXPathAnonymizeXmlFilter 
 * SingleXPathPruneXmlFilter
 * MultiXPathXmlFilter
 * SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter
 * SingleXPathPruneMaxNodeLengthStAXXmlFilter

and also

 * SingleXPathAnonymizeMaxNodeLengthXmlFilter 
 * SingleXPathPruneMaxNodeLengthXmlFilter
 * MultiXPathMaxNodeLengthXmlFilter
 
with max node size set to MAX_INTEGER. Again a reference implementation, 
 
 * DefaultXmlFilter

is included. This filter simply copies from input to output and serves as a minimum threshold and sanity check. Run using command


```
java -Ddirectory=./src/test/resources/soap -jar target/benchmarks.jar SoapHeaderBench
```

runs for a few minutes.

[JMH]: http://openjdk.java.net/projects/code-tools/jmh/
[Aalto XML]: https://github.com/FasterXML/aalto-xml