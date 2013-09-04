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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redhat.vmtruckloader.vmware.VMWareMachineService;
import org.redhat.vmtruckloader.vmware.VMWareManagedObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.ConcurrentAccess;
import com.vmware.vim25.DVPortgroupConfigInfo;
import com.vmware.vim25.DistributedVirtualSwitchPortConnection;
import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidDatastore;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceBackingInfo;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCardDistributedVirtualPortBackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualVmxnet3;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.DistributedVirtualPortgroup;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VmwareDistributedVirtualSwitch;

/**
 * @author Romain Pelisse - <romain@redhat.com>
 *
 */
public class MoveVirtualMachineToNetworkCallback extends AbstractVMWareActionCallback<Void> { //NOPMD, can't reduce the number of catches

    private static final Logger LOGGER = LoggerFactory.getLogger(VMWareMachineService.class);

    private String netName;
    private String name;

    public MoveVirtualMachineToNetworkCallback(String netName, String name) {
    	super();
        this.netName = netName;
        this.name = name;
    }

    @Override
    public Void doInVmware(ServiceInstance serviceInstance) {
        final VirtualMachine vm = checkVmConfigState(VMWareManagedObjectUtils.getVm(serviceInstance, name));

        final String[] virtualPortConfig = findEthernetConfig(serviceInstance, netName);
        if ( virtualPortConfig.length < 2 )
            throw new IllegalArgumentException("The netname provided [" + netName + "] is incorrect");
       final String portGroupKey = virtualPortConfig[0];
       final String switchUuid = virtualPortConfig[1];

        final VirtualDeviceConfigSpec ethernet = retrieveAndUpdateEthernetPort(vm.getConfig().getHardware().getDevice(),
                createVirtualPortBackingInfo(portGroupKey,switchUuid));
        if ( ethernet == null )
            invalidState("No ethernet found for VM:" + name + ", can't move it to " + netName);

        ethernet.setOperation(VirtualDeviceConfigSpecOperation.edit);

        final VirtualMachineConfigSpec spec = new VirtualMachineConfigSpec();
        spec.setDeviceChange(new VirtualDeviceConfigSpec[] { ethernet } );
        reconfigureVM(vm, spec);
        return null;
    }

    private static void reconfigureVM(VirtualMachine vm, VirtualMachineConfigSpec spec) { //NOPMD, can't reduce the number of catches
        try {
            vm.reconfigVM_Task(spec);
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
            throw new IllegalArgumentException(e);
        } catch (InsufficientResourcesFault e) {
            throw new IllegalStateException(e);
        } catch (RuntimeFault e) {
            throw new IllegalStateException(e);
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    private static VirtualDeviceConfigSpec retrieveAndUpdateEthernetPort(VirtualDevice[] devices, VirtualDeviceBackingInfo netBackingInfo) {
        VirtualDeviceConfigSpec ethernet = null;
        for ( VirtualDevice device : devices ) {
            if ( device instanceof VirtualVmxnet3 ) {
                final VirtualVmxnet3 xnet3 = (VirtualVmxnet3) device;
                xnet3.setBacking(netBackingInfo);
                ethernet = specFromDevice(xnet3);
            }
        }
        return ethernet;
    }

    private static void invalidState(final String mssg) {
        LOGGER.warn(mssg);
        throw new IllegalStateException(mssg);
    }

    private VirtualMachine checkVmConfigState(VirtualMachine vm) {
        if ( vm == null || vm.getConfig() == null || vm.getConfig().getHardware() == null || vm.getConfig().getHardware().getDevice() == null) {
            invalidState("VM " + name + " has no config or hardware infos. Something is wrong, aborting moving the VM to VLAN" + netName);
        }
        return vm;
    }
    
    private static VirtualEthernetCardDistributedVirtualPortBackingInfo createVirtualPortBackingInfo(String portGroupKey, String switchUuid) {
		final VirtualEthernetCardDistributedVirtualPortBackingInfo netBacking = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
		final DistributedVirtualSwitchPortConnection port = new DistributedVirtualSwitchPortConnection();
		port.setPortgroupKey(portGroupKey);
		port.setSwitchUuid(switchUuid);
		netBacking.setPort(port);
		return netBacking;
	}
    
	private static VirtualDeviceConfigSpec specFromDevice(VirtualDevice device) {
		final VirtualDeviceConfigSpec spec = new VirtualDeviceConfigSpec();
		spec.setOperation(VirtualDeviceConfigSpecOperation.add);
		spec.setDevice(device);
		return spec;
	}

	/**
	 * find port group key and virtual switch uuid for a network
	 * @param vc           the virtual center
	 * @param networkName  the network name
	 * @return String array with port group key and switch uuid
	 */
	private String[] findEthernetConfig(ServiceInstance serviceInstance, String networkName) {
		String networkNameEncoded = networkName.replaceAll("/", "%2f"); // slashes have to be encoded :-/
		System.out.println("NetworkName:" + networkName);
		Map<String, VmwareDistributedVirtualSwitch> switches = new HashMap<String, VmwareDistributedVirtualSwitch>();				
		List<ManagedEntity> switchList = VMWareManagedObjectUtils.getManagedEntities(serviceInstance,VmwareDistributedVirtualSwitch.class.getSimpleName());
		for (ManagedEntity me: switchList) {
			VmwareDistributedVirtualSwitch sw = (VmwareDistributedVirtualSwitch)me;
			switches.put(sw.getMOR().get_value(), sw);
			System.out.println("Switch found:" + sw.getName() + "[" + sw.getNetworkResourcePool() + "]");
		}

		List<ManagedEntity> managedEntities = VMWareManagedObjectUtils.getManagedEntities(serviceInstance,DistributedVirtualPortgroup.class.getSimpleName());
		for (ManagedEntity me: managedEntities) {
			DistributedVirtualPortgroup g = (DistributedVirtualPortgroup)me;
			if (g.getName().equals(networkNameEncoded)) {
				String switchKey = ((DVPortgroupConfigInfo)g.getConfig()).getDistributedVirtualSwitch().get_value();
				VmwareDistributedVirtualSwitch sw = switches.get(switchKey);
				System.out.println("VirtualSwitch found:" + g.getName() + "[" + g.getKey() + "]");
				return new String[] { g.getKey(), sw.getUuid() };
			}
		}
		return new String[0];
	}

}
