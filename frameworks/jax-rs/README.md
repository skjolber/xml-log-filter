# xml-log-filter-jax-rs
JAX-RS annotation for pretty-printing of XML in RESTful services.

## Obtain
The project is based on [Maven] and is available on the central Maven repository.

Example dependency config:

```xml
<dependency>
    <groupId>com.github.skjolber.xml-log-filter</groupId>
    <artifactId>xml-log-filter-jax-rs</artifactId>
    <version>1.0.0</version>
</dependency>
```
# Usage
Use the `XmlLogPrinter` annotation to enable logging of XML in incoming and/or outgoing request bodies. The body `MediaType` main type must be either `text` or `application` and the subtype must contain `xml`. Requests are logged with INFO level. Non-XML requests are logged as text.

## Class annotation
Add the `XmlLogPrinter` annotation to a class and it applies to all resource methods.

```java
@Path("myResource")
@XmlLogPrinter
public class MyResource {
    ...
}
```

## Method annotation
Add the `XmlLogPrinter` annotation to a method.

```java
@Path("myRequestPath")
@Produces("application/xml")
@Consumes("application/xml")
@POST
@XmlLogPrinter
public MyResponseObject myRequest(MyRequestObject r) { // JAXB-annotated example objects
    ...
}
```

## Details
The `XmlLogPrinter` annotation supports parameters corresponding to the [DefaultXmlFilterFactory] found in the core artifact.

### Max CDATA node sizes
Configuring

```java
@XmlLogPrinter(maxCDATANodeLength = 1024, maxTextNodeLength = 1024)
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
@XmlLogPrinter(anonymizeFilters = {"/parent/child"}) // multiple paths supported
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
@XmlLogPrinter(pruneFilters = {"/parent/child"}) // multiple paths supported
```

results in

```xml
<parent>
	<child>
	    <!-- [SUBTREE REMOVED] -->
    </child>
</parent>
```

See the core artifact for supported XPath expressions and various limitations.

