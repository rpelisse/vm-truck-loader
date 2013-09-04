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

import com.vmware.vim25.mo.ServiceInstance;

import org.redhat.vmtruckloader.vmware.action.VMWareActionCallback;

/**
 * Helper class that simplifies performing actions in VMWare.
 * <p/>
 * This class manages the lifecycle VMWare {@link ServiceInstance ServiceInstances} (i.e. connections).
 * <p/>
 * This implementation is based on the Spring <code>HibernateTemplate</code> and <code>HibernateCallback</code> 
 * architecture (although a lot simpler). 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 * @author Romain Pelisse - romain@redhat.com
 */
public class VMWareTemplate {
	
	@Inject
	private VMWareConnectionManager connectionManager;

	private String username;
	private String password;
	private URL vCenterURL;

	public void setCredential(String username, String password, URL vCenterURL) {
		this.username = username;
		this.password = password;
		this.vCenterURL = vCenterURL;
	}
	
	public <T> T execute(VMWareActionCallback<T> action)  {
		return doExecute(action);
	}
	
	private <T> T doExecute(VMWareActionCallback<T> callback) {
		T result = null;
		final ServiceInstance serviceInstance = connectionManager.getServiceInstance(username, password,vCenterURL);
		try {
			result = callback.doInVmware(serviceInstance);
		} finally {
			connectionManager.closeServiceInstance(serviceInstance);
		}
		return result;
	}
}
