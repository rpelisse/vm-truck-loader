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

package org.redhat.vmtruckloader.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * 
 * @author Romain Pelisse - romain@redhat.com
 *
 */
public class VirtualMachineUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualMachineUtils.class);
    
	public static String getMacAddress(VirtualMachine vm) {
		if ( vm == null )
			throw new IllegalArgumentException("Provided instance of " + VirtualMachine.class.getName() + " was 'null'.");
		
        final List<String> macAddresses = getMacAddresses(vm);
        if (macAddresses.isEmpty() )
            throw new IllegalStateException("Machine " + vm.getName() + " has no @MAC address !");
        return macAddresses.get(0);
    }

	public static List<String> getMacAddresses(VirtualMachine vm) {

        final VirtualMachineConfigInfo vmConfigInfo = vm.getConfig();
        if ( vmConfigInfo == null ) throw new IllegalStateException("No ConfigInfo for VM:" + vm.getName());
        if ( vmConfigInfo.getHardware() == null ) throw new IllegalStateException("No Hardware for VM:" + vm.getName());
        if ( vmConfigInfo.getHardware().getDevice() == null ) throw new IllegalStateException("No Device for VM:" + vm.getName());

        final VirtualDevice[] vdev= vmConfigInfo.getHardware().getDevice();
        if ( vdev.length == 0 )
            return Collections.emptyList();
        else {
            final List<String> macAddresses = new ArrayList<String>(vdev.length);
            for(int idDevice=0; idDevice < vdev.length; idDevice++) {
                if((vdev[idDevice] instanceof VirtualEthernetCard)) {
                    final String macAddress = ((VirtualEthernetCard) vdev[idDevice]).macAddress;
                    macAddresses.add(macAddress);
                    if ( LOGGER.isDebugEnabled() )
                        LOGGER.debug("@MAC:" + macAddress);
                }
            }
            return macAddresses;
        }
    }
}
