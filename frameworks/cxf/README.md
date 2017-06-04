# xml-log-filter-cxf
Pretty-printing CXF logging feature implementation using [xml-log-filter].

## Obtain
The project is based on [Maven] and is not yet available on the central Maven repository.

Example dependency config:

```xml
<dependency>
    <groupId>com.github.skjolber.xml-log-filter</groupId>
    <artifactId>xml-log-filter-cxf</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

# Usage
Add 

	 com.github.skjolber.xmlfilter.cxf.LoggingFeature

as a feature. Refer to [CXF features documentation] for details, or see the examples.

## Details
The component includes two additional interceptors intended for subclassing:

	com.github.skjolber.xmlfilter.cxf.XMLLoggingOutInterceptor

and

	com.github.skjolber.xmlfilter.cxf.XMLLoggingInInterceptor

These simplify the process of logging only specific request attributes, like message id and such, and are generally less verbose than the default CXF interceptor.

[CXF features documentation]: http://cxf.apache.org/docs/features.html
