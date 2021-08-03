# xml-log-filter
High-performance filtering of to-be-logged XML. Reads, filters, formats and writes XML in a single step -  drastically increasing throughput. Typical use-cases

  * Filter sensitive values from logs 
     * technical details like passwords and so on
     * sensitive personal information, for [GDPR](https://en.wikipedia.org/wiki/General_Data_Protection_Regulation) compliance and such
  * Remove big elements (i..e base64 encoded binary data) from logs
     * low or no informational value
     * consuming unnecessary log accumulation tool resources

In a typical bare-bones system, this could translate to something like 5-10% overall performance improvement.

Features:

  * [High-performance] filtering of XML
    * Max text and/or CDATA node sizes
    * Anonymize of element and/or attribute contents
    * Removal of subtrees
    * Indenting (pretty-printing) for use in testing
  * Custom processors
    * SOAP header filter
  * Support for popular frameworks
    * CXF 
    * JAX-RS
  * Examples
    * Spring
    * CXF

The processors have all been validated to handle valid documents using the [latest] W3C XML test suite.

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

## License
[Apache 2.0]

## Obtain
The project is built with [Maven] and is available on the central Maven repository.

```xml
<dependency>
    <groupId>com.github.skjolber.xml-log-filter</groupId>
    <artifactId>xml-log-filter-core</artifactId>
    <version>1.0.5</version>
</dependency>
```

# Usage
See individual sub-modules for detailed usage instructions and examples.

### Max CDATA node sizes
Configuring

```java
factory.setMaxTextNodeLength(1024);
factory.setMaxCDATANodeLength(1024);
```

yields output like (at a smaller max length)

```xml
<parent>
    <child><![CDATA[QUJDREVGR0hJSktMTU5PUFFSU1...[TRUNCATED BY 46]]]></child>
</parent>
```

for CDATA and correspondingly for text nodes.

### Anonymizing attributes and/or elements
Configuring

```java
factory.setAnonymizeFilters(new String[]{"/parent/child"}); // multiple paths supported
```

results in 

```xml
<parent>
    <child>[*****]</child>
</parent>
```

See below for supported XPath syntax.

### Removing subtrees
Configuring

```java
factory.setPruneFilters(new String[]{"/parent/child"}); // multiple paths supported
```

results in

```xml
<parent>
    <child><!-- [SUBTREE REMOVED] --></child>
</parent>
```

See below for supported XPath syntax.

### XPath expressions
A minor subset of the XPath syntax is supported. However multiple expressions can be used at once. Namespace prefixes in the XML are simply ignored, only local names at used to determine a match. Expressions are case-sensitive.

#### Anonymize 
Supported syntax:

    /my/xml/element
    /my/xml/@attribute

with support for wildcards; 

    /my/xml/*
    /my/xml/@*

or a simple any-level element search 

    //myElement

which cannot target attributes.

#### Prune
Supported syntax:

    /my/xml/element

with support for wildcards; 

    /my/xml/*

or a simple any-level element search 

    //myElement

## Performance
The processors within this project are much faster than stock processors. This is expected as parser/serializer features have been traded for performance. 

The project has DOM- and StAX-based equivalents for feature and performance comparison. 
Depending on the implementation, benchmarks show throughput is approximately 5x-10x compared to stock processors. 

Memory use will be approximately two times the XML string size.

See this [visualization] and the [JMH] module for running detailed benchmarks.

## Background
The project is intended as a complimentary tool for use alongside XML frameworks, such as SOAP- or XML-based REST stacks. Its primary use-case is processing to-be logged XML. The project relies on the fact that such frameworks have very good error handling, like schema validation, to apply a simplified view of the XML syntax, basically handling only the happy-case of a well-formed document. The frameworks themselves detect invalid documents and handle them as raw content. 

# See also
See projects

 * [xml-formatter] for additional indenting/formatting of inner XML. 
 * [json-log-filter] for filtering of JSON.

# History
- [1.0.5]: Support for CXF 3.4, with good help from [TomEvers](https://github.com/TomEvers). CXF 3.3 users: Use version 1.0.4.
- 1.0.4: Maintenance release
- 1.0.3: Maintenance release
- 1.0.2: Initial Java 11 (modules) support.

[1.0.5]:                releases
[Aalto]:                https://github.com/FasterXML/aalto-xml
[Apache 2.0]:           http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:        https://github.com/skjolber/xml-log-filter/issues
[Maven]:                http://maven.apache.org/
[latest]:               https://www.w3.org/XML/Test/
[JMH]:                  benchmark/jmh
[xml-formatter]:        https://github.com/greenbird/xml-formatter-core
[visualization]:	https://jmh.morethan.io/?source=https://raw.githubusercontent.com/skjolber/xml-log-filter/master/docs/benchmark/jmh-result.json&topBar=off
[High-performance]:	https://jmh.morethan.io/?source=https://raw.githubusercontent.com/skjolber/xml-log-filter/master/docs/benchmark/jmh-result.json&topBar=off
 [json-log-filter]: https://github.com/skjolber/json-log-filter
