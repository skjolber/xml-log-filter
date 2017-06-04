# xml-log-filter-jax-rs-example
JAX-RS example using XmlLogFilter annotation to configure logging for a REST-service in Spring Boot.

# Usage
Build using maven command

	mvn clean package
	
then deploy resulting war package on Apache Tomcat version 7 or any other suitable container. Then access

	http://${host}:${port}/xml-log-filter-jax-rs-example/myResource/myMethod
	

