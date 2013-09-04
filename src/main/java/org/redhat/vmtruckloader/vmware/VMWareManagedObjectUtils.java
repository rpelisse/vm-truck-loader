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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * 
 * @author Romain Pelisse - romain@redhat.com
 *
 */
public class VMWareManagedObjectUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(VMWareManagedObjectUtils.class);
	
	private static final boolean WMWARE_FREESPACE_RECO_ENABLED = true;
	
	public  static List<ManagedEntity> getManagedEntities(InventoryNavigator inventoryNavigator, String entityClass) {
		List<ManagedEntity> managedEntities = new ArrayList<ManagedEntity>();

		try {
			for (ManagedEntity me : inventoryNavigator.searchManagedEntities(entityClass)) {
				managedEntities.add(me);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
            throw new IllegalArgumentException(e);
		}

		return managedEntities;
	}

    public  static List<ManagedEntity> getManagedEntities(ServiceInstance serviceInstance, String entityClass) {
        List<ManagedEntity> managedEntities = new ArrayList<ManagedEntity>();

        try {
			InventoryNavigator inventoryNavigator = new InventoryNavigator(serviceInstance.getRootFolder());
            for (ManagedEntity me : inventoryNavigator.searchManagedEntities(entityClass)) {
                 managedEntities.add(me);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }

        return managedEntities;
    }

	public  static ResourcePool findResourcePoolByName(ServiceInstance serviceInstance, String resourcePoolName) {
		List<ManagedEntity> pools = getManagedEntities(serviceInstance, ResourcePool.class.getSimpleName());
		for ( ManagedEntity resourcePool : pools) {
			if ( resourcePool instanceof ResourcePool ) {
				ResourcePool pool = (ResourcePool)resourcePool;
				if ( pool.getName().equals(resourcePool.getName()))
					return pool;
			}
		}
		return null;
	}

	public  static Folder lookVmFolder(ServiceInstance serviceInstance, String foldername) {
		Folder vmFolder = null;
		List<ManagedEntity> folders = getManagedEntities(serviceInstance,Folder.class.getSimpleName());
		for ( ManagedEntity folder : folders) {
			String mssg = "- " + folder.getName();
			if ( folder instanceof Folder ) {
				mssg += ", parent:" + ((Folder)folder).getParent();
				if ( foldername.equals(folder.getName()))
					vmFolder = (Folder)folder;
			}
			if ( LOGGER.isDebugEnabled() ) LOGGER.debug(mssg);
		}
		
		if ( vmFolder == null ) {
			LOGGER.error("No folder named " + foldername + " found");
			throw new IllegalArgumentException("No folder named " + foldername + " found");
		}
		return vmFolder;
	}
	
	/**
	 * Find datastore with max available space in a given storage pod for a new
	 * virtual machine with diskSize bytes. VMWare recommends to use data stores
	 * up to 90%.
	 * 
	 * @param storagePodName
	 *            the storage pod name to use
	 * @param vc
	 *            the virtual data center
	 * @param diskSize
	 *            required disk size (in KB)
	 * @return a datastore or null if none found
	 */
	public static Datastore findDataStore(String storagePodName,
			int diskSize, ServiceInstance instance) {
		Datastore targetDS = null;
		final List<Datastore> dss = getDatastores(instance);
		long maxSize = 0;
		for (Datastore ds : dss) {
			if (ds.getName().equals(storagePodName)) {
				final long usableSize = ds.getSummary().getFreeSpace() * 90 / 100; // VMWare
																					// recommendation
				maxSize = usableSize;
				targetDS = ds;
				if (usableSize > maxSize && usableSize >= diskSize * 1024) {
					final String errorMssg = "Datastore " + storagePodName
							+ " found, but not with enough free space:"
							+ ds.getSummary().getFreeSpace();
					LOGGER.warn(errorMssg);
					if (WMWARE_FREESPACE_RECO_ENABLED)
						throw new IllegalArgumentException("Datastore "
								+ storagePodName
								+ " found, but not with enough free space:"
								+ ds.getSummary().getFreeSpace());
				}
			}
		}
		if (targetDS == null)
			throw new IllegalArgumentException("No storage found with name "
					+ storagePodName + ".");
		return targetDS;
	}
	
	
	private static List<Datastore> getDatastores(ServiceInstance serviceInstance) {
		final List<Datastore> datastores = new ArrayList<Datastore>();

		try {
			final InventoryNavigator inventoryNavigator = new InventoryNavigator(
					serviceInstance.getRootFolder());
			for (ManagedEntity me : inventoryNavigator
					.searchManagedEntities(Datastore.class.getSimpleName())) {
				if ( LOGGER.isDebugEnabled())
					LOGGER.debug("Datastore found:" + me.getName());
				datastores.add((Datastore) me);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}

		return datastores;
	}
	
	public static VirtualMachine getVm(ServiceInstance serviceInstance, String vmName) {
		VirtualMachine vm = null;
		try {
			Folder rootFolder = serviceInstance.getRootFolder();
			InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);

			vm = (VirtualMachine) inventoryNavigator.searchManagedEntity(VirtualMachine.class.getSimpleName(), vmName);
		} catch (RemoteException re) {
			String errorMessage = "Error retrieving VirtualMachine with name: '" + vmName + "'.";
			LOGGER.error(errorMessage, re);

		}
		return vm;
	}
}
