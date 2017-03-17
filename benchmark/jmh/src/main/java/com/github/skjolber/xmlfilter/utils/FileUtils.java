package com.github.skjolber.xmlfilter.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * For testing only
 * 
 * @author thomas
 *
 */

public class FileUtils {

	private static FileDirectoryCache directoryCache = new FileDirectoryCache();
	
	private static Map<File, String> fileCache = new ConcurrentHashMap<File, String>();

	private static Map<String, URL> urlCache = new ConcurrentHashMap<String, URL>();

	private static Map<String, File> resourceCache = new ConcurrentHashMap<String, File>();

	public static String read(File file) throws FileNotFoundException, IOException {
		
		if(fileCache.containsKey(file)) {
			return fileCache.get(file);
		}
		
		if(!file.exists()) {
			throw new IllegalArgumentException(file.toString());
		}
		
		long size = file.length();
		
		byte[] buffer = new byte[(int) size];
		FileInputStream fin = new FileInputStream(file);
		DataInputStream din = new DataInputStream(fin);
		try {
			din.readFully(buffer);
		} finally {
			din.close();
		}
		
		String value = new String(buffer, Charset.forName("UTF-8"));
		
		// attempt to detect encoding, if present in header
		if(value.startsWith("<?xml ")) {
			int end = value.indexOf("?>");
			
			int encodingStart = value.indexOf("encoding=\"");
			if(encodingStart != -1 && encodingStart < end) {
				int encodingEnd = value.indexOf('"', encodingStart + 11);
				
				if(encodingEnd != -1 && encodingEnd < end) {
					String encoding = value.substring(encodingStart + 10, encodingEnd);
					if(!encoding.toLowerCase().equals("utf-8")) {
						value = new String(buffer, Charset.forName(encoding));
					}
				}
			}
		}
		
		fileCache.put(file, value);
		
		return value;
	}
	
	public static final String read(String resource) throws Exception {
		File file = new File(resource);
		return read(file);
		
	}
	
	public static URL getResourceAsURL(String resource) {
		if(urlCache.containsKey(resource)) {
			return urlCache.get(resource);
		}
		
		ClassLoader cl = FileUtils.class.getClassLoader();
		URL url = cl.getResource(resource);
		
		urlCache.put(resource, url);
		
		return url;
	}
	
	public static File getResourcesAsFile(String resource) {
		if(resourceCache.containsKey(resource)) {
			return resourceCache.get(resource);
		}
		
		URL inputURL = getResourceAsURL(resource);
		if(inputURL == null) {
			throw new RuntimeException(resource);
		}
		File file = new File(inputURL.getFile());

		resourceCache.put(resource, file);

		return file;
	}

	public static FileDirectoryValue getValue(String input, final FileFilter filter) throws FileNotFoundException, IOException {
		return directoryCache.getValue(input, filter);
	}
}
