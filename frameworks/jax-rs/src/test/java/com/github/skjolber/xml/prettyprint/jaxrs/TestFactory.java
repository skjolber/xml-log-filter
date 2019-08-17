package com.github.skjolber.xml.prettyprint.jaxrs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogFilter;
import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogFilterAnnotationFactory;
import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.core.MaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneXmlFilter;
import com.github.skjolber.xmlfilter.core.XmlIndentationFilter;

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
    	
    	Assert.assertNotNull(factory.getXmlFilter(null));
     }
    
    @Test
    public void testDefault() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();
    	
    	XmlFilter xmlFilter = factory.getXmlFilter(Defaults.of(XmlLogFilter.class));
    	Assert.assertNotNull(xmlFilter);
    	Assert.assertSame(xmlFilter, factory.getXmlFilter(null));
    }

    @Test
    public void testParametersXmlDeclaration() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.xmlDeclaration()).thenReturn(Boolean.FALSE);
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	Assert.assertTrue(result.getClass().getName(), result instanceof MaxNodeLengthXmlFilter);
    }

    @Test
    public void testParametersIgnoreWhitespace() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.indent()).thenReturn(Boolean.TRUE);
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	Assert.assertTrue(result.getClass().getName(), result instanceof XmlIndentationFilter);
    }

    @Test
    public void testParametersPrune() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.pruneFilters()).thenReturn(new String[]{"/a"});
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	Assert.assertTrue(result.getClass().getName(), result instanceof SingleXPathPruneXmlFilter);
    }
    
    @Test
    public void testParametersAnonymize() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.anonymizeFilters()).thenReturn(new String[]{"/a"});
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	Assert.assertTrue(result.getClass().getName(), result instanceof SingleXPathAnonymizeXmlFilter);
    }

    @Test
    public void testParametersMaxCDATANodeLength() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.maxTextNodeLength()).thenReturn(-1);
        when(mock.maxCDATANodeLength()).thenReturn(1024);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	Assert.assertTrue(result.getClass().getName(), result instanceof MaxNodeLengthXmlFilter);
    }

    @Test
    public void testParametersMaxTextNodeLength() {
    	XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

    	XmlLogFilter mock = mock(XmlLogFilter.class);
        when(mock.maxTextNodeLength()).thenReturn(1024);
        when(mock.maxCDATANodeLength()).thenReturn(-1);
    	
    	XmlFilter result = factory.getXmlFilter(mock);
    	Assert.assertTrue(result.getClass().getName(), result instanceof MaxNodeLengthXmlFilter);
    }

 
    
    
    
}
