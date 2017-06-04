# JMH benchmarks

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

Runtime is a few (4-5) minutes.

## Example results
All benchmarks should be taken with a grain of salt; the below results give hints to the true performance, but not absolute truth.
Saving a decent amount of time on a small operation will not necessarily improve the overall system performance.

### SoapHeaderBench.
Average time, floored.

| Benchmark                                                           | Score ±  Error (ns/op)  |
| ------------------------------------------------------------------- | ----------------------- |
| filter_dom_w3cDomXPathXmlFilter                                     | 1153793 ± 2608 | 
| filter_multiXPathMaxNodeLengthXmlFilter                             | 270549 ± 1255  | 
| filter_multiXPathXmlFilter                                          | 266102 ± 2160  | 
| filter_noop_passthrough                                             | 230876 ± 1095  | 
| filter_singleXPathAnonymizeMaxNodeLengthXmlFilter                   | 253404 ±  598  | 
| filter_singleXPathPruneMaxNodeLengthXmlFilter                       | 254527 ± 1627  | 
| filter_singleXPathPruneXmlFilter                                    | 246637 ± 1077  | 
| filter_singleXPathXmlFilter                                         | 259887 ± 1637  | 
| filter_soapheader_singleXPathAnonymizeSoapHeaderXmlFilter           | 230177 ± 1698  | 
| filter_soapheader_singleXPathPruneSoapHeaderXmlFilter               | 269108 ± 2769  | 
| filter_soapheader_stax_singleXPathAnonymizeStAXSoapHeaderXmlFilter  | 423603 ± 1816  | 
| filter_soapheader_stax_singleXPathPruneStAXSoapHeaderXmlFilter      | 447423 ± 5663  | 
| filter_stax_singleXPathAnonymizeMaxNodeLengthStAXXmlFilter          | 432579 ± 1635  | 
| filter_stax_singleXPathPruneMaxNodeLengthStAXXmlFilter              | 462964 ± 6687  | 

### IndentBenchmark
Average time, floored.

| Benchmark                                         | Score ±  Error (ns/op)  |
| --------------------------------------------------| ----------------------- |
| filter_noop_passthrough                           | 228328 ±  4849 |
| indent_dom_w3cDomXmlIndentationFilter             | 987627 ± 10710 |
| indent_filter_maxNodeLengthXmlIndentationFilter   | 257310 ± 11611 |
| indent_filter_multiXPathXmlIndentationFilter      | 296749 ±  9246 |
| indent_filter_singleXPathXmlIndentationFilter     | 273716 ±  6337 |
| indent_filter_xmlIndentationFilter                | 253629 ±  5315 |
| indent_reference_aaltoStaxXmlIndentationFilter    | 578140 ± 15793 |
| indent_reference_cxfXmlIndentationFilter          | 737174 ±  6440 |
| indent_reference_defaultStAXXmlIndentationFilter  | 572491 ±  3891 |
| indent_reference_transformXmlIndentationFilter    | 711115 ± 10045 |
| indent_reference_w3cDomXPathXmlIndentationFilter  | 1453416 ± 11747 |
| indent_reference_xercesSAXXmlIndentationFilter    | 763993 ±  5803 |

[JMH]: http://openjdk.java.net/projects/code-tools/jmh/
[Aalto XML]: https://github.com/FasterXML/aalto-xml