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
package org.redhat.vmtruckloader.vmware; //NOPMD, class is long but pretty simple, refactoring not needed

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.redhat.vmtruckloader.service.MachineService;
import org.redhat.vmtruckloader.service.MachineSpecification;
import org.redhat.vmtruckloader.vmware.action.AbstractVMWareActionCallback;
import org.redhat.vmtruckloader.vmware.action.CloneMachineCallback;
import org.redhat.vmtruckloader.vmware.action.DeleteMachineActionCallback;
import org.redhat.vmtruckloader.vmware.action.EditMachineCallback;
import org.redhat.vmtruckloader.vmware.action.GetMachineActionCallback;
import org.redhat.vmtruckloader.vmware.action.GetMachinesActionCallback;
import org.redhat.vmtruckloader.vmware.action.MoveVirtualMachineToNetworkCallback;
import org.redhat.vmtruckloader.vmware.action.StartMachineActionCallback;
import org.redhat.vmtruckloader.vmware.action.StopMachineActionCallback;
import org.redhat.vmtruckloader.vmware.action.VMWareActionCallback;

import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * <p>VMWare implementation of the {@link MachineService} responsible for {@link Machine}
 * related semantics (list, start, stop, etc.).</p>
 *
 * @author Duncan Doyle - duncan.doyle@redhat.com
 * @author Romain Pelisse - belaran@redhat.com
 */
public class VMWareMachineService implements MachineService { //NOPMD, class is long but pretty simple, refactoring not needed

	@Inject
	private VMWareTemplate vmwareTemplate;
	
	@Override
	public void setConnectionData(String username, String password, URL vCenterUrl) {
		vmwareTemplate.setCredential(username, password, vCenterUrl);
	}

	public Collection<VirtualMachine> getMachines() {
		return vmwareTemplate.execute(new GetMachinesActionCallback());
	}

	@Override
	public VirtualMachine getMachine(final String machineName) {
		return vmwareTemplate.execute(new GetMachineActionCallback(machineName));
	}

	@Override
	public synchronized void startMachine(String machineName) { //NOPMD, block level sync is not an option here
		vmwareTemplate.execute(new StartMachineActionCallback(machineName, true));
	}

	@Override
	public synchronized void stopMachine(String machineName) { //NOPMD, block level sync is not an option here
		stopMachine(machineName,true);
	}

	public synchronized void stopMachine(String machineName, boolean async) { //NOPMD, block level sync is not an option here
		vmwareTemplate.execute(new StopMachineActionCallback(machineName, async));
	}

	@Override
	public void deleteMachine(String machineName) {
		vmwareTemplate.execute(new DeleteMachineActionCallback(machineName));
	}

	@Override
	public MachineSpecification cloneMachine(String sourceMachineName, MachineSpecification spec) {
		return vmwareTemplate.execute(new CloneMachineCallback(sourceMachineName, spec));
	}

	@Override
	public Collection<Task> getJobs() {
		final VMWareActionCallback<Collection<Task>> callback = new AbstractVMWareActionCallback<Collection<Task>>() {

			@Override
			public Collection<Task> doInVmware(ServiceInstance serviceInstance) {
				Task[] recentTasks = serviceInstance.getTaskManager().getRecentTasks();
				List<Task> tasks = new ArrayList<Task>(recentTasks.length);
				for ( Task task: recentTasks ) {
					tasks.add(task);
				}
				return tasks;
			}

		};
		return vmwareTemplate.execute(callback);
	}

	@Override
	public void deploy(String netName, String vmName) {
		vmwareTemplate.execute(new MoveVirtualMachineToNetworkCallback(netName, vmName));
	}

	@Override
	public void editMachine(MachineSpecification machineCreate) {
		vmwareTemplate.execute(new EditMachineCallback(machineCreate));
	}
}
