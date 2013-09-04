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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.UserSession;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.SessionManager;


/**
 * Very basic implementation of the {@link VMWareConnectionManager}.
 * <p/>
 * This implementation currently does not provide any connection 
 * pooling, session pooling, etc.  
 *
 * @author Romain Pelisse - romain@redhat.com
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
@Singleton
public class VMWareConnectionManager implements Serializable {

	private static final long serialVersionUID = -3523778360566486478L;

	private Logger logger = LoggerFactory.getLogger(VMWareConnectionManager.class.getName());
	
	public ServiceInstance getServiceInstance(String username, String password, URL vCenterURL) {
		return getServiceInstanceInternal(new VMWareContext(username, password, vCenterURL));
	}

	public void closeServiceInstance(ServiceInstance serviceInstance) {
		logout(serviceInstance);
	}

	private static final String ERROR_MESSSAGE = "Unable to connect to the VMWare back-end:";
	private ServiceInstance getServiceInstanceInternal(VMWareContext context) {
		ServiceInstance serviceInstance = null;
		try {
			serviceInstance = new ServiceInstance(context.getVCenterURL(), context.getUsername(), context.getPassword(), true);
		} catch (RemoteException re) {
			logger.error(ERROR_MESSSAGE, re);
			throw new IllegalStateException(ERROR_MESSSAGE, re);
		} catch (MalformedURLException mue) {
			logger.error("URL to VMWare backend is invalid " + mue, mue);
			throw new IllegalStateException("Server appears badly configured, please contact admins.");
		}
		return serviceInstance;
	}

	private void logout(ServiceInstance serviceInstance) {
		final SessionManager sessionManager = serviceInstance.getSessionManager();
		final UserSession session = sessionManager.getCurrentSession();
		if (session != null) {
			try {
				sessionManager.logout();
			} catch (RemoteException e) {
				logger.error("Error while closing VMWare ServiceInstance connection.");
				//Not much we can do here, so not throwing any exceptions.
			}
		}
	}
}