package com.github.skjolber.xmlfilter.servlet.example.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.core.MaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.servlet.example.endpoints.LoggerServlet;

@Configuration
@PropertySource("classpath:application.properties")
public class ServletConfiguration {

	@Value("${servlet.path}")
	private String servletUrl;

	@Bean
	public ServletRegistrationBean<LoggerServlet> servlet1() {
		return setXmlFilter(new MaxNodeLengthXmlFilter(false, 128, 128));
	}

	public ServletRegistrationBean<LoggerServlet> setXmlFilter(XmlFilter xmlFilter) {
		ServletRegistrationBean<LoggerServlet> bean = new ServletRegistrationBean<>(new LoggerServlet(xmlFilter), String.format(servletUrl, xmlFilter.getClass().getSimpleName()));
		bean.setName(xmlFilter.getClass().getSimpleName());
		return bean;
	}

}
