package com.github.skjolber.ddom.stream.sax;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rometools.rome.io.XmlReader;

public class XMLConformanceEntityResolver implements EntityResolver {

	private String basePath;
	
	public XMLConformanceEntityResolver(URL url) {
		this.basePath = parent(url.toExternalForm());
	}
	
	private String parent(String path) {
		if(path.equals("/")) {
			return null;
		}
		int index = path.lastIndexOf('/');
		return path.substring(0, index);
	}

	public InputSource resolveEntity(String publicId, String systemID) throws IOException, SAXException {
		if (systemID.contains("://") || systemID.startsWith("jar:file") || systemID.startsWith("file:/")) {
			try {
				InputStream openStream = new URL(systemID).openStream();
				if (openStream != null) {
					return readStringInputSource(openStream);
				}
			} catch(FileNotFoundException e) {
				// ignore
			}
		}
		
		String directory = basePath;

		// how far back are we, if any?
		int count = 0;
		do {
			if (systemID.startsWith(directory)) {
				break;
			}
			directory = parent(directory);
			
			count++;
		} while (directory != null);

		String path = systemID.substring(directory.length() + 1);
		
		int i = 0;
		do {
			URL fileURL = new URL(basePath + "/" + path);
			
			try {
				InputStream openStream = fileURL.openStream();
				if (openStream != null) {
					return readStringInputSource(openStream);
				}
			} catch(FileNotFoundException e) {
				// ignore
			}
			
			path = "../" + path;
		} while (i++ < count);

		return null;
	}

	public static InputSource readStringInputSource(InputStream in) throws IOException {
		return new InputSource(new XmlReader(in));
	}
}
