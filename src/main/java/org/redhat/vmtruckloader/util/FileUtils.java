/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.redhat.vmtruckloader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 
 * @author Romain Pelisse - romain@redhat.com
 *
 */
public final class FileUtils {

	private final static String DEFAULT_FILE_ENCODING = "UTF-8";
	
	private FileUtils() {}
	
	public static Reader constructReaderFromClasspathResource(final String filename) {
		try {
			return new InputStreamReader(FileUtils.getClassLoader().getResourceAsStream(filename));
		} catch ( Exception e) {
			final String errorMssg = "Can't read file " + filename + " from classloader.";
			throw new IllegalStateException(errorMssg,e);
		}		
	}
	
	private static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	public static String getFileEncoding() {
        String fileEncoding = System.getProperty("file.encoding");
        if ( fileEncoding == null || "".equals(fileEncoding))
         	fileEncoding = DEFAULT_FILE_ENCODING;       	// Fallback to UTF-8 default encoding
        return fileEncoding;
	}

	public static boolean exists(String filename) {
		return new File(filename).exists();
	}

    public static void write(String filename, String text) {
        String fileEncoding = FileUtils.getFileEncoding();
        Writer out = null;
        try {
        	out = new OutputStreamWriter(new FileOutputStream(filename), fileEncoding);
            out.write(text);
            out.flush();
			if (out != null )
				out.close();
        } catch (IOException e) {
			throw new IllegalStateException(e);
		}
    }
    
    public static String read(String fileName) {
        String fileEncoding = getFileEncoding();
        StringBuilder text = new StringBuilder();

        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(fileName), fileEncoding);
            while (scanner.hasNextLine()) {
                text.append(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
        	throw new IllegalStateException(e);
		} finally {
        	if ( scanner != null )
        		scanner.close();
        }
        return text.toString();
    }
	

    public static List<String> readLineByLine(File file) {
    	if ( file == null )
    		throw new IllegalArgumentException("Provided was file object was 'null'");
    	if ( ! file.exists())
    		throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist");
    	if ( ! file.canRead() )
    		throw new IllegalArgumentException("File " + file.getAbsolutePath() + " can't be read");

    	return readLineByLine(file.getAbsoluteFile().toString());
    }

    public static List<String> readLineByLine(String fileName) {
        String fileEncoding = getFileEncoding();        
        List<String> lines = new ArrayList<String>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(fileName), fileEncoding);
            while (scanner.hasNextLine()) {
            	lines.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
        	throw new IllegalStateException(e);
		} finally {
        	if ( scanner != null )
        		scanner.close();
        }
        return lines;
    }

    
	public static void delete(String outputFilename) {
		final File file = new File(outputFilename);
		if ( file.exists() && file.canWrite() && file.isFile() ) file.delete();
	}
}