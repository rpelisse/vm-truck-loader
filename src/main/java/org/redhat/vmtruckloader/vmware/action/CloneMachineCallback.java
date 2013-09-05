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

import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * Clone a VM - not really tested...
 *
 * @author Duncan Doyle - <ddoyle@redhat.com>
 * @author Romain Pelisse - <romain@redhat.com>
 *
 */
public class CloneMachineCallback extends AbstractVMWareActionCallback<MachineSpecification> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloneMachineCallback.class);
	private final String sourceMachineName;
	private MachineSpecification spec;

	public CloneMachineCallback(final String sourceMachineName, MachineSpecification machineSpecification) {
		this.sourceMachineName = sourceMachineName;
		this.spec = machineSpecification;
	}

	private VirtualMachineRelocateSpec buildRelocateSpec(ServiceInstance serviceInstance) {
		VirtualMachineRelocateSpec relocateSpec = new VirtualMachineRelocateSpec();

		final String mssg = "Can't retrieve ResourcePool named " + spec.getResourcePoolName();
		ResourcePool pool = VMWareManagedObjectUtils.findResourcePoolByName(serviceInstance, spec.getResourcePoolName());
		if ( pool == null ) {
			LOGGER.error(mssg);
			throw new IllegalArgumentException(mssg);
		}
		relocateSpec.pool = pool.getMOR();

		Datastore store = VMWareManagedObjectUtils.findDataStore(spec.getDatastoreName(), spec.getDiskSize(), serviceInstance);
		relocateSpec.datastore = store.getMOR();

		return relocateSpec;
	}

	private void specDebugInfo(VirtualMachineCloneSpec cloneSpec, VirtualMachine sourceVm) {
		if ( LOGGER.isDebugEnabled() ) {
			LOGGER.debug("- sourceVm.getParent():" +  sourceVm.getParent());
			LOGGER.debug("- name:" +  spec.getHostname() );
			LOGGER.debug("- cloneSpec:" +  cloneSpec.toString() );
		}
	}

	private void retrieveAndRemoveExistingEthernetCard(VirtualMachine sourceVm,VirtualMachineCloneSpec cloneSpec ) {
        if ( sourceVm == null )
            throw new IllegalArgumentException("SourceVM is 'null', can't clone VM");
		for ( VirtualDevice device : sourceVm.getConfig().getHardware().getDevice() ) {
			if ( LOGGER.isDebugEnabled() )
				LOGGER.debug("Device found:" + device.getDeviceInfo().getLabel() + "[" + device.getClass() + "]");
			if ( device instanceof VirtualEthernetCard )  {
				VirtualEthernetCard nic = (VirtualEthernetCard) device;
				VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
				nicSpec.setDevice(nic);
				nicSpec.setOperation(VirtualDeviceConfigSpecOperation.remove);
				cloneSpec.getConfig().deviceChange[1] = nicSpec;
			}
		}
	}

	@Override
	public MachineSpecification doInVmware(ServiceInstance serviceInstance) {
		VirtualMachineRelocateSpec relocateSpec = buildRelocateSpec(serviceInstance);

		VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
		cloneSpec.template = false;
		cloneSpec.powerOn = false;
		cloneSpec.location = relocateSpec;
		cloneSpec.setConfig(VMWareMachineSpecsUtils.buildVmSpec(serviceInstance, this.spec));		
		
		VirtualMachine sourceVm = VMWareManagedObjectUtils.getVm(serviceInstance, sourceMachineName);
		retrieveAndRemoveExistingEthernetCard(sourceVm, cloneSpec);
		Task task;
		try {
			specDebugInfo(cloneSpec, sourceVm);
			Folder setupFolder = VMWareManagedObjectUtils.lookVmFolder(serviceInstance, "SETUP");
			
			task = sourceVm.cloneVM_Task(setupFolder, spec.getHostname(), cloneSpec);
			LOGGER.debug("TaskKey = " + task.getTaskInfo().key);

			try {
				String status = task.waitForTask();
				if ( ! Task.SUCCESS.equals(status))
					throw new IllegalStateException("Operation fails (status:" + status + ") - resulting clone, if any, may not be in a consistent state.");
			} catch (InterruptedException ie) {
				LOGGER.error("Interrupted while waiting for cloning of machine '" + sourceMachineName + "' to '"
						+ spec.getHostname() + "' in resource pool '" + spec.getHostname() + "'.", ie);
				Thread.currentThread().interrupt();
			}
			return new VirtualMachineTransformer(spec).getMacAddress(VMWareManagedObjectUtils.getVm(serviceInstance, spec.getVmName()));
		} catch (RemoteException re) {
			String errorMessage = "Error while cloning '" + sourceMachineName + "' to '" + spec.getHostname() + "' in resource pool '"
					+ spec.getResourcePoolName() + "'.";
			LOGGER.error(errorMessage, re);
			throw new IllegalArgumentException(errorMessage, re);
		}
	}
}
