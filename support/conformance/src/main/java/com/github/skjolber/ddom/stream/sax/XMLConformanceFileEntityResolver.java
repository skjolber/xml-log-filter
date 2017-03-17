package com.github.skjolber.ddom.stream.sax;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rometools.rome.io.XmlReader;

public class XMLConformanceFileEntityResolver implements EntityResolver {

	private File base;

	public XMLConformanceFileEntityResolver(File base) {
		this.base = base;
	}

	public InputSource resolveEntity(String publicId, String systemID) throws IOException, SAXException {
		if (systemID.contains("://")) {
			InputStream openStream = new URL(systemID).openStream();
			if (openStream != null) {
				return readStringInputSource(openStream);
			}
		}

		// relative paths are translated directly, i.e. we will see the parent
		// directory, not the ..
		File file;
		if (systemID.startsWith("file://")) {
			file = new File(systemID.substring(7));
		} else {
			file = new File(systemID);
		}

		File directory = new File(".").getAbsoluteFile().getParentFile();

		// how far back are we, if any?
		int count = 0;
		do {
			if (file.getAbsolutePath().startsWith(directory.getAbsolutePath())) {
				break;
			}

			directory = directory.getParentFile();
			count++;
		} while (directory != null);

		String path = file.getAbsolutePath().substring(directory.getAbsolutePath().length() + 1);

		// try to make it fit for all cases
		int i = 0;
		do {
			file = new File(base.getParent(), path);
			if (file.exists()) {
				return readStringInputSource(new FileInputStream(file));
			}

			path = "../" + path;
		} while (i++ < count);

		return null;
	}
	
	public static InputSource readStringInputSource(InputStream in) throws IOException {
		return new InputSource(new XmlReader(in));
	}	

}
