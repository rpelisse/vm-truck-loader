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

import org.redhat.vmtruckloader.service.MachineSpecification;
import org.redhat.vmtruckloader.vmware.VMWareMachineSpecsUtils;
import org.redhat.vmtruckloader.vmware.VMWareManagedObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.ConcurrentAccess;
import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidDatastore;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * <p>Callback in charge of creating a new Machine</p>
 *
 * @author Romain Pelisse - <romain@redhat.com>
 *
 */
public class EditMachineCallback extends AbstractVMWareActionCallback<VirtualMachine> {

	private static final Logger LOGGER = LoggerFactory.getLogger(EditMachineCallback.class);

	private final MachineSpecification spec;

	public EditMachineCallback(MachineSpecification spec) {
		this.spec = spec;
	}

	@Override
	public VirtualMachine doInVmware(ServiceInstance serviceInstance) {		
		return runTask(doesVMexistsAndCanItBeModified(serviceInstance, spec), VMWareMachineSpecsUtils.buildVmSpecAndSetCpusAndMemory(serviceInstance, spec));
	}

	private VirtualMachine doesVMexistsAndCanItBeModified(ServiceInstance serviceInstance, MachineSpecification machineCreateWrapper) {
		final VirtualMachine vm = VMWareManagedObjectUtils.getVm(serviceInstance, machineCreateWrapper.getVmName());
		if ( vm == null )
			throw new IllegalArgumentException("No VM with name:" + machineCreateWrapper.getVmName() + " (rename operation are not supported).");
		if ( vm.getSummary().getRuntime().getPowerState().toString() == "poweredOn" )
			throw new IllegalArgumentException(" can't change the hardware settings for a running vm ... Stop vm first.");	
		checkIfVMisNotStillDeploying(vm.getConfig().getExtraConfig());
		return vm;
	}
	
	private void checkIfVMisNotStillDeploying(OptionValue[] optionsValues) {	
		for ( OptionValue optionValue : optionsValues ) {
			if ( optionValue.getKey().equals("guestinfo.deploy.lock") && optionValue.getValue().equals("true") ) {
				throw new IllegalArgumentException("VM " + spec.getVmName() +  
						" is still in deployment state, please finish deployment first before changing hardware settings");
			}
		}
	}

	private VirtualMachine runTask(VirtualMachine vm, VirtualMachineConfigSpec vmSpec) {
		try {
			Task task = vm.reconfigVM_Task(vmSpec);
			task.waitForTask();
		} catch (InvalidName e) {
			throw new IllegalArgumentException(e);
		} catch (VmConfigFault e) {
			throw new IllegalArgumentException(e);
		} catch (DuplicateName e) {
			throw new IllegalArgumentException(e);
		} catch (TaskInProgress e) {
			throw new IllegalStateException(e);
		} catch (FileFault e) {
			throw new IllegalStateException(e);
		} catch (InvalidState e) {
			throw new IllegalStateException(e);
		} catch (ConcurrentAccess e) {
			throw new IllegalStateException(e);
		} catch (InvalidDatastore e) {
			throw new IllegalStateException(e);
		} catch (InsufficientResourcesFault e) {
			throw new IllegalStateException(e);
		} catch (RuntimeFault e) {
			throw new IllegalStateException(e);
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			if ( LOGGER.isWarnEnabled() )
				LOGGER.warn("Remote Task on vCenter timeout.");
		}
		return vm;
	}
}