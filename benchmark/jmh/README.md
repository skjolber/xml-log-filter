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

or

```
java -Ddirectory=./src/test/resources/soap -jar target/benchmarks.jar -rf json ".*Benchmark.*"
```

for all benchmark. Display with a visualizer like [JMH Visualizer].

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

# FilterBenchmark
Combines an XPath expression with max node length of 127.

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

See this [visualization] for interactive results. Summarized below.

### FilterBenchmark 

| Benchmark                               | Score     |
| --------------------------------------- | --------- |
| DOM                                     | 1k        | 
| StAX                                    | 4.5k      | 
| xml-log-filter                          | 26-62K    | 
| passthrough                             | 403k      | 

### IndentBenchmark
Average time, floored.

| Benchmark                               | Score     |
| --------------------------------------- | --------- |
| DOM                                     | 0.8-1.2k  | 
| SAX                                     | 1.4-2k    |
| StAX                                    | 2.8k      | 
| xml-log-filter                          | 11.5-22.4K| 
| passthrough                             | 404k      | 


### SoapHeaderBench
Average time, floored.

| Benchmark                               | Score     |
| --------------------------------------- | --------- |
| DOM                                     | 0.9k      | 
| SAX                                     | 4.4k      |
| StAX                                    | 4-4.5k    | 
| xml-log-filter                          | 27.7-51.8K| 
| xml-log-filter app-specific             | 186-192K  | 
| passthrough                             | 403k      | 

[JMH]: 		https://openjdk.java.net/projects/code-tools/jmh/
[Aalto XML]: 		https://github.com/FasterXML/aalto-xml
[JMH Visualizer]:	https://jmh.morethan.io/
[visualization]:	https://jmh.morethan.io/?source=https://raw.githubusercontent.com/skjolber/xml-log-filter/master/docs/benchmark/jmh-result.json&topBar=off
