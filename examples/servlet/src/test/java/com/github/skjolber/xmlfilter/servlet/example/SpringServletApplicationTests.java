package com.github.skjolber.xmlfilter.servlet.example;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.skjolber.xmlfilter.servlet.example.SimpleBootServletApplication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SimpleBootServletApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration
@DirtiesContext
public class SpringServletApplicationTests {

	private XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    @Test
    public void testLongTextNode() throws Exception {
    	
    	byte[] image = IOUtils.toByteArray(getClass().getResourceAsStream("/images/holygrail.jpg"));

    	URL url = new URL("http://localhost:8080/servlet/MaxNodeLengthXmlFilter");
    	
    	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    	
    	connection.setDoInput(true);
    	connection.setDoOutput(true);
    	
    	connection.setRequestProperty("Content-Type", "text/xml");
    	
    	OutputStream outputStream = connection.getOutputStream();
    	
    	XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);
    	
    	writer.writeStartElement("image");
    	writer.writeCharacters(Base64.getEncoder().encodeToString(image));
    	writer.writeEndElement();
    	
    	writer.close();
    	
    	int responseCode = connection.getResponseCode();
    	assertEquals(200, responseCode);
    }
    
}