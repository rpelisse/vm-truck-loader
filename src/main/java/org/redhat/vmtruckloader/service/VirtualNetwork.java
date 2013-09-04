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
 * <p>A simple POJO class designed to contains all the information relating
 * to one virtual netork. This allows smoother API, and ease extension - as
 * one can easily add extra object to this class, without breaking the 
 * existing mehtod signature.</p>
 * 
 * @author Romain Pelisse - <romain@redhat.com>
 *
 */
@Data
public class VirtualNetwork {
	
	private final String networkName;
	private final String portGroupKey;
	private final String switchUuid;
	
	public VirtualNetwork(String network, String portGroupKey, String switchUuid) {
		this.networkName = network;
		this.portGroupKey = portGroupKey;
		this.switchUuid = switchUuid;
	}
}