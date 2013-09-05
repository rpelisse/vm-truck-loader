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

import lombok.Data;


/**
 * A simple POJO to hold the specification of a Virtual Machine.
 * @author Romain Pelisse - belaran@gmail.com
 *
 */
@Data
public class MachineSpecification {

	public MachineSpecification() {}
	
	public MachineSpecification(String machineName) {
		this.hostname = machineName;
	}
	private String env;
	private String hostname;
	private String role;
	private String MAC;
	private String ipAddress;
	private String VLAN;
	private String resourcePoolName;
	private String datastoreName;
	private String folder;
	private int nbCpu;
	private int vRAM;
	private int diskSize = 20;
	
	public String getVmName() {
		return hostname;
	}

	public MachineSpecification addVLAN(String string) {
		this.VLAN = string;
		return this;		
	}

	public MachineSpecification addRole(String string) {
		role = string;
		return this;
	}
	
	public MachineSpecification addEnv(String env) {
		this.env = env;
		return this;
	}

	public MachineSpecification addIpAddress(String string) {
		ipAddress = string;
		return this;
	}

	public MachineSpecification addResourcePool(String string) {
		resourcePoolName = string;
		return this;
	}

	public MachineSpecification addDatastore(String datastoreName) {
		this.datastoreName = datastoreName;
		return this;
	}

	public MachineSpecification addCpus(int i) {
		this.nbCpu = i;
		return this;
	}

	public MachineSpecification addRam(int ram) {
		this.vRAM = ram;
		return this;
	}

	public MachineSpecification addMacAddress(String macAddress) {
		this.MAC = macAddress;
		return this;
	}
}