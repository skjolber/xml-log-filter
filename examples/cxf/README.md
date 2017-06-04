# xml-log-filter-cxf-spring-boot-example
Example project with CXF logging interceptors implemented as SOAP webservices in Spring Boot.

# Usage
Build using maven command

	mvn clean package
	
then start the resulting app using command

	mvn spring-boot:run -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Drun.jvmArguments="-Xmx2048m -Xms256m"

Then access the various services at port 8080.

## Details
Make a SOAP request as follows:

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:log="http://xmlns.github.skjolber.com/schema/logger">
	<soapenv:Header />
	<soapenv:Body>
		<log:performLogMessageRequest>
			<!-- your XML here -->
		</log:performLogMessageRequest>
	</soapenv:Body>
</soapenv:Envelope>
```

and the service responds with

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
   <soap:Body>
      <log:performLogMessageResponse xmlns:log="http://xmlns.github.skjolber.com/schema/logger">
			<!-- your XML here -->
      </log:performLogMessageResponse>
   </soap:Body>
</soap:Envelope>
```
