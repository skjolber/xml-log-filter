package com.github.skjolber.xml.prettyprint.jaxrs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.skjolber.xmlfilter.XmlFilter;

@Provider
@XmlLogFilter
public class XmlLogContainerFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmlLogContainerFilter.class);

	public static final String LOG_PRETTY_PRINTER = XmlLogContainerFilter.class.getName() + ":xmlFilter";

	private XmlLogFilterAnnotationFactory factory = new XmlLogFilterAnnotationFactory();

	@Context
	private ResourceInfo resourceInfo;

	protected Logger logger;

	public XmlLogContainerFilter() {
		this(LOGGER);
	}

	public XmlLogContainerFilter(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		MultivaluedMap<String, Object> headers = responseContext.getHeaders();
		Object contentLength = headers.getFirst("Content-Length");

		int contentSize;
		if(responseContext.hasEntity()) {
			if(contentLength != null) {
				contentSize = Integer.parseInt((String)contentLength);
			} else {
				contentSize = -1;
			}
		} else {
			contentSize = 0;
		}

		StringBuilder buffer;
		if(contentSize == -1) {
			buffer = new StringBuilder(16 * 1024 + 1024);
		} else {
			buffer = new StringBuilder(contentSize + 1024);
		}

		String method = requestContext.getMethod();

		buffer.append(method);
		buffer.append(' ');
		buffer.append(requestContext.getUriInfo().getPath());
		buffer.append(" [response]");

		if(responseContext.hasEntity()) {
			XmlFilter filter = (XmlFilter) requestContext.getProperty(LOG_PRETTY_PRINTER);
			
			// pass everything through the stream, seems properties are not propagated the
			// same way by all framework implementations
			responseContext.setEntityStream(new CacheStream(responseContext.getEntityStream(), buffer, filter));
		} else {
			logger.info(buffer.toString());
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		MultivaluedMap<String, String> headers = requestContext.getHeaders();
		String contentLength = headers.getFirst("Content-Length");

		int contentSize;
		if(requestContext.hasEntity()) {
			if(contentLength != null) {
				contentSize = Integer.parseInt(contentLength);
			} else {
				contentSize = -1;
			}
		} else {
			contentSize = 0;
		}

		StringBuilder buffer;
		if(contentSize == -1) {
			buffer = new StringBuilder(8 * 1024 + 1024);
		} else {
			buffer = new StringBuilder(contentSize + 1024);
		}

		String method = requestContext.getMethod();
		String path = requestContext.getUriInfo().getPath();

		buffer.append(method);
		buffer.append(' ');
		buffer.append(path);
		buffer.append(" [request]");

		if(requestContext.hasEntity()) {
			XmlFilter filter = getXmlFilter(requestContext);

			requestContext.setProperty(LOG_PRETTY_PRINTER, filter);

			InputStream entityStream = requestContext.getEntityStream();

			int markBuilderLength = buffer.length();

			InputStream result = readInputStream(entityStream, contentSize, buffer, getCharset(requestContext.getMediaType()));

			requestContext.setEntityStream(result);

			char[] chars = new char[buffer.length()];
			buffer.getChars(markBuilderLength, buffer.length(), chars, 0);
			buffer.setLength(markBuilderLength);

			if(isXML(requestContext.getMediaType())) {
				// pretty print XML
				if(filter.process(chars, 0, chars.length, buffer)) {
					// log as normal
					logger.info(buffer.toString());
				} else {
					// something unexpected - log as exception
					buffer.append(" was unable to format XML\n");
					buffer.append(chars); // unmodified XML

					logger.warn(buffer.toString());
				}
			} else {
				buffer.append('\n');
				buffer.append(chars); // textual content

				logger.info(buffer.toString());
			}
		} else {
			logger.info(buffer.toString());
		}
	}

	public static InputStream readInputStream(InputStream entityStream, int contentSize, StringBuilder builder, Charset charset) throws IOException {
		ByteArrayOutputStream bout;
		if(contentSize != -1) {
			bout = new ByteArrayOutputStream(contentSize);
		} else {
			bout = new ByteArrayOutputStream(8 * 1024);
		}

		if(entityStream.markSupported() && contentSize != -1) {
			entityStream.mark(contentSize);
		}

		byte[] readBuffer = new byte[8 * 1024];
		int read;
		do {
			read = entityStream.read(readBuffer);
			if(read == -1) {
				break;
			}
			bout.write(readBuffer, 0, read);

		} while(true);

		InputStream result;
		if(entityStream.markSupported() && bout.size() == contentSize) {
			entityStream.reset();

			result = entityStream;
		} else {
			result = new ByteArrayInputStream(bout.toByteArray());
		}

		builder.append(bout.toString(charset.name()));

		return result;
	}

	private XmlFilter getXmlFilter(ContainerRequestContext requestContext) {
		XmlLogFilter methodAnnotation = resourceInfo.getResourceMethod().getAnnotation(XmlLogFilter.class);

		// method overrides the class annotation in full
		if(methodAnnotation != null) {
			return factory.getXmlFilter(methodAnnotation);
		} 

		XmlLogFilter classAnnotation = resourceInfo.getResourceClass().getAnnotation(XmlLogFilter.class);

		return factory.getXmlFilter(classAnnotation);
	}

	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

		OutputStream outputStream = context.getOutputStream();
		
		context.proceed();
		if (outputStream instanceof CacheStream) {
			CacheStream cacheStream = (CacheStream)outputStream;
			XmlFilter prettyPrinter = cacheStream.getFilter();
			if(prettyPrinter == null) {
				prettyPrinter = getXmlFilter(context.getAnnotations());
			}

			String content = cacheStream.getCacheOutputStream().toString(getCharset(context.getMediaType()).name());

			StringBuilder buffer = cacheStream.getBuffer();
			
			if(isXML(context.getMediaType())) {
				// pretty print XML
				if(prettyPrinter.process(content, buffer)) {
					// log as normal
					logger.info(buffer.toString());
				} else {
					// something unexpected - log as exception
					buffer.append(" was unable to format XML\n");
					buffer.append(content); // unmodified XML

					logger.warn(buffer.toString());
				}
			} else {
				buffer.append('\n');
				buffer.append(content); // textual content

				logger.info(buffer.toString());
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	protected boolean isXML(MediaType mediaType) {
		if(mediaType == null) {
			return false;
		}
		return (mediaType.getType().equals("text") || mediaType.getType().equals("application")) && mediaType.getSubtype().contains("xml");
	}

	private XmlFilter getXmlFilter(Annotation[] annotations) {
		XmlLogFilter prettyPrint = null;
		if(annotations != null) {
			for(Annotation annotation : annotations) {
				if(annotation instanceof XmlLogFilter) {
					prettyPrint = (XmlLogFilter)annotation;

					break;
				}
			}
		}

		return factory.getXmlFilter(prettyPrint);
	}

	public static class CacheStream extends OutputStream {

		private final OutputStream delegate;
		private final ByteArrayOutputStream cacheOutputStream = new ByteArrayOutputStream();
		private final StringBuilder buffer;
		private final XmlFilter filter;
		
		public CacheStream(final OutputStream delegate, StringBuilder buffer, XmlFilter filter) {
			this.delegate = delegate;
			this.buffer = buffer;
			this.filter = filter;
		}
		
		public XmlFilter getFilter() {
			return filter;
		}

		public StringBuilder getBuffer() {
			return buffer;
		}
		
		@Override
		public void write(final int i) throws IOException {
			cacheOutputStream.write(i);
			delegate.write(i);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			cacheOutputStream.write(b, off, len);
			delegate.write(b, off, len);
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}

		@Override
		public void flush() throws IOException {
			delegate.flush();
		}

		public ByteArrayOutputStream getCacheOutputStream() {
			return cacheOutputStream;
		}
	}

	/**
	 * Get the character set from a media type.
	 * <p>
	 * The character set is obtained from the media type parameter "charset".
	 * If the parameter is not present the UTF8 charset is utilized.
	 *
	 * @param m the media type.
	 * @return the character set.
	 */
	public static Charset getCharset(MediaType m) {
		String name = (m == null) ? null : m.getParameters().get("charset");
		return (name == null) ? Charset.forName("UTF-8") : Charset.forName(name);
	}
}