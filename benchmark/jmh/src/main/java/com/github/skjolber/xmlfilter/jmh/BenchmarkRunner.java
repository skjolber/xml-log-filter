package com.github.skjolber.xmlfilter.jmh;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import com.github.skjolber.xmlfilter.utils.FileDirectoryCache;
import com.github.skjolber.xmlfilter.utils.FileDirectoryValue;
import com.skjolberg.xmlfilter.XmlFilter;

public class BenchmarkRunner {
	
	private List<FileDirectoryValue> directories;
	private XmlFilter xmlFilter;
	
	public BenchmarkRunner(File file, boolean recursive) throws IOException {
		directories = new FileDirectoryCache().getValue(file, new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.getName().toLowerCase().endsWith(".xml");
			}
		}, recursive);
	}

	public XmlFilter getXmlFilter() {
		return xmlFilter;
	}

	public void setXmlFilter(XmlFilter xmlFilter) {
		this.xmlFilter = xmlFilter;
	}

	public long benchmark() {
		StringBuilder builder = new StringBuilder(1024 * 1024);

		// warmup
		long sizeSum = 0;
		for(FileDirectoryValue directory : directories) {
			
			for(int i = 0; i < directory.size(); i++) {
				char[] xmlChars = directory.getValue(i);
			
				if(xmlFilter.process(xmlChars, 0, xmlChars.length, builder)) {
					sizeSum += builder.length();
				} else {
					throw new RuntimeException("Unable to pretty-print using " + xmlFilter + " for source " + directory.getFile(i));
				}
				
				// reset builder for next iteration
				builder.setLength(0);
			}
		}
		if(sizeSum == 0) {
			throw new IllegalArgumentException();
		}
		return sizeSum;
	}
}
