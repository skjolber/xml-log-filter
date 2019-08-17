# xml-log-filter-api
Base classes for interaction with this library. 

 * `XmlFilter` - basic filter input / output interface
 * `XmlFilterFactory` - set properties and create corresponding XmlFilters 

The actual implementation of `XmlFilterFactory` depends on your use-case, but for exampel

```java
XmlFilterFactory xmlFilterFactory = DefaultXmlFilterFactory.newInstance();
```

```xml
<dependency>
    <groupId>com.github.skjolber.xml-log-filter</groupId>
    <artifactId>xml-log-filter-conformance<</artifactId>
    <version>1.0.2</version>
</dependency>
```


