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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidDatastore;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.OutOfBounds;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.Task;

/**
 * @author Romain Pelisse - <romain@redhat.com>
 *
 * @param <T> - type returned by the callback method
 */
public abstract class AbstractVMWareActionCallback<T> implements VMWareActionCallback<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVMWareActionCallback.class);

	protected void runTask(String errorMssg) {
		Task task;
		try {
			task = runTask();
			String status = task.waitForTask();
			if ( status != Task.SUCCESS ) {
				TaskInfo info = task.getTaskInfo();
				LOGGER.warn(info.getError().localizedMessage);
				throw new IllegalStateException(errorMssg);
			}
		} catch (InvalidName e) {
			throw new IllegalArgumentException(e);
		} catch (VmConfigFault e)  {
			throw new IllegalArgumentException(e);
		} catch (DuplicateName e) {
			throw new IllegalArgumentException(e);
		} catch (FileFault e) {
			throw new IllegalStateException(e);
		} catch (OutOfBounds e) {
			throw new IllegalStateException(e);
		} catch (InsufficientResourcesFault e) {
			throw new IllegalStateException(e);
		} catch (InvalidDatastore e) {
			throw new IllegalArgumentException(e);
		} catch (RuntimeFault e) {
			throw new IllegalStateException(e);
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			LOGGER.warn("Wait for task timed out - task may not have finished successfully:" + e.getLocalizedMessage());
		} catch (Exception e ) {
			LOGGER.warn("Unknown Exception:" + e.getMessage());
			throw new IllegalStateException(e);
		}
	}

	protected Task runTask() throws InvalidName, VmConfigFault, DuplicateName, FileFault, OutOfBounds, InsufficientResourcesFault, InvalidDatastore, RuntimeFault, RemoteException {
		throw new UnsupportedOperationException("Callback " + this.getClass() + " has invoked runTaks but not override it!");
	}

}
