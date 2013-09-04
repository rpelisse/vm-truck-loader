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
package org.redhat.vmtruckloader.service;

import java.net.URL;
import java.util.Collection;

import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * Service interface for the <code>MachineService</code>.
 * <p/>
 * Provides functionality to get machine info, start a machine, stop a machine, etc.
 * 
 * @author Romain Pelisse - belaran@gmail.com
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public interface MachineService {

	/**
	 * <p>Allow to pass to the instance the credential information for the backend
	 * system.</p>
	 * 
	 * @param username
	 * @param password
	 */
	public void setConnectionData(String username, String password, URL vCenterURL);
	/**
	 * Retrieves the {@link Machine Machines}.
	 *
	 * @return a {@link Collection} containing machines.
	 */
	public Collection<VirtualMachine> getMachines();

	/**
	 * Retrieves the {@link Machine} information of the given machine.
	 *
	 * @param machineName
	 *            the name of the machine.
	 * @return the {@link Machine} information.
	 */
	public abstract VirtualMachine getMachine(String machineName);

	/**
	 * Starts the machine with the given name.
	 *
	 * @param machineName
	 *            the name of the machine that needs to be started.
	 */
	public abstract void startMachine(String machineName);

	/**
	 * Stops the machine with the given name.
	 *
	 * @param machineName
	 *            the name of the machine that needs to be stopped.
	 */
	public abstract void stopMachine(String machineName);

	/**
	 * Stops the machine with the given name.
	 *
	 * @param machineName
	 *            the name of the machine that needs to be stopped.
	 */
	public abstract void deleteMachine(String machineName);

	/**
	 * Clones the source machine to the target machine.
	 *
	 * @param machineName
	 *            the name of the machine that needs to be stopped.
	 */
	public abstract MachineSpecification cloneMachine(String sourceMachineName, MachineSpecification spec);

	/**
	 * Returns currently running jobs. This operation can be used to check status of long-running jobs like 'clone', etc.
	 *
	 * @return a {@link Collection} of {@link Job Jobs}.
	 */
	public abstract Collection<Task> getJobs();


	/**
	 * Move a VM to the appropriate VLAN
	 */
	public abstract void deploy(String netName, String vmName);
	
	/**
	 * @param machines spec
	 */
	public void editMachine(MachineSpecification machineCreate);
	
}
