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

import org.redhat.vmtruckloader.service.MachineSpecification;

import com.vmware.vim25.Description;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualVmxnet3;
import com.vmware.vim25.mo.ServiceInstance;

/**
 * 
 * @author Romain Pelisse - romain@redhat.com
 *
 */
public class VMWareMachineSpecsUtils {

	private final static long GIGABYTE = 1024L;
	
	private final static String NETWORK_INTERFACE_NAME = "Network Adapter 1";
	private final static String NETWORK_ADDRESS_TYPE = "generated";

	public static VirtualMachineConfigSpec buildVmSpec(ServiceInstance serviceInstance, MachineSpecification spec) {
		VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
		vmSpec.setName(spec.getHostname());
		vmSpec.setNumCPUs(spec.getNbCpu());
		vmSpec.setMemoryMB(spec.getVRAM() * GIGABYTE);
		vmSpec.setAnnotation(spec.getRole());

		final String netName = spec.getVLAN();
		VirtualDeviceConfigSpec ethernet = createNicSpec(netName,VirtualDeviceConfigSpecOperation.add);
		
		vmSpec.setDeviceChange(new VirtualDeviceConfigSpec[] { ethernet,null });
		return vmSpec;
	}
	
	private static VirtualDeviceConfigSpec createNicSpec(String netName, VirtualDeviceConfigSpecOperation operation) {
		final String nicName = NETWORK_INTERFACE_NAME;
		VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
		nicSpec.setOperation(operation);

		VirtualEthernetCard nic = new VirtualVmxnet3();
		VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
		nicBacking.setDeviceName(netName);

		Description info = new Description();
		info.setLabel(nicName);
		info.setSummary(netName);
		nic.setDeviceInfo(info);

		// type: "generated", "manual", "assigned" by VC
		nic.setAddressType(NETWORK_ADDRESS_TYPE);
		nic.setBacking(nicBacking);
		nic.setKey(0);	

		nicSpec.setDevice(nic);
		return nicSpec;
	}

	
	public  static VirtualMachineConfigSpec buildVmSpecAndSetCpusAndMemory(ServiceInstance serviceInstance, MachineSpecification spec) {
		VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
		vmSpec.setName(spec.getVmName());
						
		if ( spec.getNbCpu() != 0 )
			vmSpec.setNumCPUs(spec.getNbCpu());
		if ( spec.getVRAM() != 0 )
			vmSpec.setMemoryMB(Long.valueOf(spec.getVRAM() * 1024));
		return vmSpec;
	}


	public  static VirtualMachineConfigSpec updateVmSpecAndSetCpusAndMemory(ServiceInstance serviceInstance, MachineSpecification spec, VirtualMachineConfigSpec vmSpec) {
		vmSpec.setName(spec.getVmName());
						
		if ( spec.getNbCpu() != 0 )
			vmSpec.setNumCPUs(spec.getNbCpu());
		if ( spec.getVRAM() != 0 )
			vmSpec.setMemoryMB(Long.valueOf(spec.getVRAM() * 1024));
		return vmSpec;
	}
}