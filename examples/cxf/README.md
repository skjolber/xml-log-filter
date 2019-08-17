# xml-log-filter-cxf-spring-boot-example
Example project with CXF logging interceptors implemented as SOAP webservices in Spring Boot.

It demonstrates truncating an embedded image in base64 form and obfuscating some secret header sessionid value. 

Resulting log statements are for request

```
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Header>
    <logHeader xmlns="http://xmlns.skjolber.github.com/schema/logger">
      <userId>userId</userId>
      <sessionId>[*****]</sessionId>
    </logHeader>
  </soap:Header>
  <soap:Body>
    <performLogMessageRequest xmlns="http://xmlns.skjolber.github.com/schema/logger">
      <image>/9j/4AAQSkZJRgABAQEAZABkAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEB...[TRUNCATED BY 133684]</image>
    </performLogMessageRequest>
  </soap:Body>
</soap:Envelope>
```

and response

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <performLogMessageResponse xmlns="http://xmlns.skjolber.github.com/schema/logger">
      <image>/9j/4AAQSkZJRgABAQEAZABkAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEB...[TRUNCATED BY 133684]</image>
    </performLogMessageResponse>
  </soap:Body>
</soap:Envelope>
```

excluding the pretty-printing.
