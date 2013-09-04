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
package org.redhat.vmtruckloader.vmware;

import java.net.URL;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very simple implementation of the VMWareContext.
 *
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 * @author Romain Pelisse - romain@redhat.com
 */
public class VMWareContext {

	@Inject
	private static final Logger LOGGER  = LoggerFactory.getLogger(VMWareContext.class);
		
	private final URL vcenterUrl;
	private String vcenterUsername;
	private String vcenterPassword;

	public VMWareContext(String username, String password,
			URL vCenterURL2) {
		vcenterUrl = vCenterURL2;
		vcenterUsername = username;
		vcenterPassword = password;
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Configured " + this.getClass().getSimpleName() + " with url '" + vcenterUrl + "'.");
		}
	}

	public URL getVCenterURL() {
		return vcenterUrl;
	}

	public String getUsername() {
		return vcenterUsername;
	}

	public String getPassword() {
		return vcenterPassword;
	}
}