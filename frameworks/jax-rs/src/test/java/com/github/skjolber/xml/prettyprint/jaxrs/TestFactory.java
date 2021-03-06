package com.github.skjolber.xml.prettyprint.jaxrs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.core.MaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneXmlFilter;
import com.github.skjolber.xmlfilter.core.XmlIndentationFilter;
import com.github.skjolber.xmlns.schema.logger.SampleRestApplication;

@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestFactory {

	private static class Defaults implements InvocationHandler {
		  public static <A extends Annotation> A of(Class<A> annotation) {
		    return (A) Proxy.newProxyInstance(annotation.getClassLoader(),
		        new Class[] {annotation}, new Defaults());
		  }
		  public Object invoke(Object proxy, Method method, Object[] args)
		      throws Throwable {
		    return method.getDefaultValue();
		  }
		
		}

    @Test
    public void testNull() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();
    	
    	assertNotNull(factory.getXmlFilter(null));
     }
    
    @Test
    public void testDefault() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();
    	
    	XmlFilter xmlFilter = factory.getXmlFilter(Defaults.of(XmlLogFilter.class));
    	assertNotNull(xmlFilter);
    	assertSame(xmlFilter, factory.getXmlFilter(null));
    }

    @Test
    public void testParametersXmlDeclaration() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.xmlDeclaration()).thenReturn(Boolean.FALSE);
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	assertTrue(result instanceof MaxNodeLengthXmlFilter, result.getClass().getName());
    }

    @Test
    public void testParametersIgnoreWhitespace() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.indent()).thenReturn(Boolean.TRUE);
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	assertTrue(result instanceof XmlIndentationFilter, result.getClass().getName());
    }

    @Test
    public void testParametersPrune() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.pruneFilters()).thenReturn(new String[]{"/a"});
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	assertTrue(result instanceof SingleXPathPruneXmlFilter, result.getClass().getName());
    }
    
    @Test
    public void testParametersAnonymize() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.anonymizeFilters()).thenReturn(new String[]{"/a"});
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	assertTrue(result instanceof SingleXPathAnonymizeXmlFilter, result.getClass().getName());
    }

    @Test
    public void testParametersMaxCDATANodeLength() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(1024);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	assertTrue(result instanceof MaxNodeLengthXmlFilter, result.getClass().getName());
    }

    @Test
    public void testParametersMaxTextNodeLength() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.maxTextNodeLength()).thenReturn(1024);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	assertTrue(result instanceof MaxNodeLengthXmlFilter, result.getClass().getName());
    }

 
    
    
    
}
