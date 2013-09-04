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
package org.redhat.vmtruckloader.vmware.action;

import java.rmi.RemoteException;

import org.redhat.vmtruckloader.vmware.VMWareManagedObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * VMWare action class which starts a machine.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class StartMachineActionCallback extends AbstractVMWareActionCallback<Void>{

	private static final Logger LOGGER = LoggerFactory.getLogger(StartMachineActionCallback.class);
	
	private final String machineName;
	
	private final boolean async;
	
	public StartMachineActionCallback(String machineName) {
		this(machineName, false);
	}
	
	public StartMachineActionCallback(String machineName, boolean async) {
		super();
		this.machineName = machineName;
		this.async = async;
	}
	
	@Override
	public Void doInVmware(ServiceInstance serviceInstance) {
		final VirtualMachine vm = VMWareManagedObjectUtils.getVm(serviceInstance, machineName);
		if (vm != null) {
			final String powerState = vm.getSummary().getRuntime().getPowerState().toString();

			if (VMWareMachinePowerState.POWERED_OFF.getState().equals(powerState)) {
				Task task;
				try {
					task = vm.powerOnVM_Task(null);
					if (!async) {
						try {
							task.waitForTask();
						} catch (InterruptedException ie) {
							LOGGER.error("Interrupted while waiting for machine to start.", ie);
							// reset the interrupt.
							Thread.currentThread().interrupt();
						}
					}
				} catch (RemoteException re) {
					final String errorMessage = "Remote error while executing 'powerOn' task on " + machineName;
					LOGGER.error(errorMessage, re);
					throw new IllegalArgumentException(errorMessage, re);
				}
			} else {
				final String errorMessage = "Cannot power-on a machine which is not powered-off. Current state of machine '" + machineName + "' is: " + powerState;
				LOGGER.warn(errorMessage);
				throw new IllegalStateException(errorMessage);
			}
		} else {
			final String errorMessage = "No machine found with name '" + machineName + "'.";
			LOGGER.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		return null;
	}
}