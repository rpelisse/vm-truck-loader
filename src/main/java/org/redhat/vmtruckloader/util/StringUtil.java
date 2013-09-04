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


/**
 * @author Romain Pelisse - <romain@redhat.com>
 *
 */
public final class StringUtil {

	private StringUtil() {}

	public static String checkIfStringValid(String string ) {
		if ( isEmpty(string) )
			throw new IllegalStateException("The following value " + string  + " is not valid.");
		return string;
	}

	public static String checkIfStringValid(String string, String errorMessage ) {
		if ( isEmpty(string) )
			throw new IllegalStateException(errorMessage);
		return string;
	}

	public static boolean isEmpty(String string) {
		return string == null || "".equals(string);
	}
}
