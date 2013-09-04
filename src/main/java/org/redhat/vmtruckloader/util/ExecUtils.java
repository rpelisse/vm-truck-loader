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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Romain Pelisse - <romain@redhat.com>
 *
 */
public final class ExecUtils {

	private ExecUtils() {};

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecUtils.class);
	public static final String COMMAND_LINE_SEPARATOR = " ";
	private static final String COMMAND_LINE_SEPARATOR_PATTERN = COMMAND_LINE_SEPARATOR + COMMAND_LINE_SEPARATOR + "*";


	public static boolean executeScript(String commandLine) {
		return (executeProcess(commandLine).exitValue() == 0 ? true : false);
	}

	public static List<String> executeScriptAndReturnsResult(String commandLine) {
		try {
			final Process process = executeProcess(commandLine);
			final int status = process.exitValue();
			if ( status != 0 ) return Collections.emptyList();
			return extractFromStream(process.getInputStream());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static String[] transformSpecIntoArray(String spec) {
		return spec.split(COMMAND_LINE_SEPARATOR_PATTERN);
	}

	private static Process executeProcess(String commandLine) {
		try {
			if ( LOGGER.isDebugEnabled() ) LOGGER.debug("Running command:" + commandLine);
			final Process process = Runtime.getRuntime().exec(transformSpecIntoArray(commandLine));
			process.waitFor();
			return process;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static List<String> extractFromStream(InputStream is) {
		final List<String> lines = new ArrayList<String>();
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = "";
			try {
				while((line = reader.readLine()) != null) { //NOPMD, this assignment is OK
					lines.add(line);
				}
			} finally {
				reader.close();
			}
		} catch(IOException ioe) {
			throw new IllegalStateException(ioe);
		}
		return lines;
	}
}
