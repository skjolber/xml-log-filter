# SOAP header filter
Filtering for SOAP requests with headers. As the headers are located in the start of the document, most of the document can be skipped. 

## Obtain
The project is based on [Maven] and is available on the central Maven repository.

```xml
<dependency>
	<groupId>com.github.skjolber.xml-log-filter</groupId>
	<artifactId>xml-log-filter-soap-header</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuration
Supported filtering 

    * Anonymize of element or attribute contents, or
    * Removal of subtrees

for a single XPath expression. In addition a count can be set, instructing the filter to skip over the remaining content whenever the filter limit has been reached.

If you need text and CDDATA max node length filtering, fall back to using generic XPath filters.

### Examples
Construct your XPath expression - see the main project README for syntax.

```java
String xpath = "/Envelope/Header/Security/UsernameToken/Password";
```

Then construct an anonymize

```java
XmlFilter filter = new SingleXPathAnonymizeSoapHeaderXmlFilter(true, xpath, 1);
```
or prune 

```java
XmlFilter filter = new SingleXPathPruneSoapHeaderXmlFilter(true, xpath, 1);
```

filter instance.

## Performance
As most of the document is not filtered, performance is approximately improved corresponding to the relative size of the header vs the body. Speedup can be considerable.

