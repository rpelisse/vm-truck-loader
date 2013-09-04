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
package org.redhat.vmtest;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.redhat.vmtruckloader.service.MachineService;
import org.redhat.vmtruckloader.service.MachineSpecification;

import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * Mock object representing a VM Ware service - work in progress
 * @author Romain Pelisse - belaran@gmail.com
 *
 */
public class MachineServiceMock implements MachineService {

	private String username;
	private String password;
	private URL vCenterURL;
	
	@Override
	public void setConnectionData(String username, String password,
			URL vCenterURL) {
		this.username = username;
		this.password = password;
		this.vCenterURL = vCenterURL;
	}

	@Override
	public Collection<VirtualMachine> getMachines() {
		List<VirtualMachine> machines = new ArrayList<VirtualMachine>();
		machines.add(getMachine("machineName"));
		return machines;
	}

	@Override
	public VirtualMachine getMachine(String machineName) {
		return new VirtualMachine(null, null);
	}

	@Override
	public void startMachine(String machineName) {}

	@Override
	public void stopMachine(String machineName) {}

	@Override
	public void deleteMachine(String machineName) {}

	@Override
	public MachineSpecification cloneMachine(String sourceMachineName,
			MachineSpecification spec) {
		spec.setMAC("@MAC");
		return spec;
	}

	@Override
	public Collection<Task> getJobs() {
		return new ArrayList<Task>(0);
	}

	@Override
	public void deploy(String netName, String vmName) {}

	@Override
	public void editMachine(MachineSpecification machineCreate) {}

	@Override
	public String toString() {
		return "MOCK - vCenter URL:" + vCenterURL + ", username" + this.username + " [" + this.password + "]";
	}
}
