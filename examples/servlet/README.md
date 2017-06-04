# xml-log-filter-servlet-example
Example project using a servlet in Spring Boot.

# Usage
Build using maven command

	mvn clean package
	
then start the resulting app using command

	mvn spring-boot:run -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Drun.jvmArguments="-Xmx2048m -Xms256m"

Then POST data to 

    http://localhost:8080/servlet/MaxNodeLengthXmlFilter
