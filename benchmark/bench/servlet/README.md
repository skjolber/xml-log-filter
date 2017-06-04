# xml-log-filter-servlet-spring-boot-bench
Project for benchmarking servlet logging interceptors in Spring. 

# Usage
Build using maven command

	mvn clean package
	
then start the resulting app using command

	mvn spring-boot:run -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Drun.jvmArguments="-Xmx2048m -Xms256m"

Then access the various services at port 8080 (see ServletConfiguration class).
