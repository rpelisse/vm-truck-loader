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

import static org.redhat.vmtruckloader.util.StringUtil.checkIfStringValid;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Romain Pelisse - <romain@redhat.com>
 *
 */
public final class URLUtils {

	private URLUtils() { }
	
	public static URI createURI(String URL) {
		if ( URL == null || "".equals(URL) )
			throw new IllegalArgumentException();
		
		try {
			return new URI(URL);
		} catch (URISyntaxException use) {
			final String errorMessage = "Invalid resource URI:" + URL;
			throw new IllegalArgumentException(errorMessage, use);
		}
	}
	
	public static URL createUrl(String urlAsString) {
		try {
			return new URL(checkIfStringValid(urlAsString));
		} catch (MalformedURLException mue) {
			final String errorMessage = "Incorrect URL:" + urlAsString;
			throw new IllegalArgumentException(errorMessage, mue);
		}		
	}

}
