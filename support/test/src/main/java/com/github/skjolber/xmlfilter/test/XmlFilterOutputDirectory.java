package com.github.skjolber.xmlfilter.test;

import java.io.File;
import java.util.List;

public class XmlFilterOutputDirectory {

	protected List<XmlFilterInputDirectory> sourceDirectories;
	protected final File directory;
	
	public XmlFilterOutputDirectory(File directory, List<XmlFilterInputDirectory> sourceDirectories) {
		this.directory = directory;
		this.sourceDirectories = sourceDirectories;
	}

	public List<XmlFilterInputDirectory> getSourceDirectories() {
		return sourceDirectories;
	}
	
	public File[] getFiles() {
		return XmlFilterInputDirectory.getFiles(directory);
	}
	
}
