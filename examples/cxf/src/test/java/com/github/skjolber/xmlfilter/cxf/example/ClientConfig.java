package com.github.skjolber.xmlfilter.cxf.example;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlns.schema.logger.LoggerPort;

@Configuration
public class ClientConfig {

    @Value("http://localhost:${server.port}${base.path}${endpoint.path}")
    private String address;

    @Bean(name = "loggerPortClientProxyBean")
    public LoggerPort opportunityPortType() {
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(LoggerPort.class);
        jaxWsProxyFactoryBean.setAddress(String.format(address, SingleXPathAnonymizeMaxNodeLengthXmlFilter.class.getSimpleName()));

        return (LoggerPort) jaxWsProxyFactoryBean.create();
    }

}