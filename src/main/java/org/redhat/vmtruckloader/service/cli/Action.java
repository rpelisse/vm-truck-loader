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
package org.redhat.vmtruckloader.service.cli;

/**
 * 
 * @author Romain Pelisse - romain@redhat.com
 *
 */
public enum Action {

	CREATE("CREATE"),EDIT("EDIT"),DELETE("DELETE"), START("START"), STOP("STOP"), RESTART("restart");
	
	private String label;

	private Action(String label) {
		this.label = label;
	}
	
	public String getLabel() { return this.label; }
	
	public static Action buildFromString(String label) {
		final String actionName = label.toUpperCase();
		for ( Action action : Action.values() ) {
			if ( action.getLabel().equals(actionName))
				return action;
		}
		throw new IllegalArgumentException("No such action named:" + label);
	}
}
