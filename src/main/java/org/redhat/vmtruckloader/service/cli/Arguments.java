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

import java.io.File;


import lombok.Data;

import com.beust.jcommander.Parameter;

/**
 * POJO to hold all the CLI arguments inputs.
 *
 * @author Romain Pelisse - romain@redhat.com
 *
 */
@Data
public class Arguments {

	@Parameter(names = { "-s", "--server-url" }, description = "URL to vCenter", required = true)
	private String vCenterURl;

	@Parameter(names = { "-u", "--username" }, description = "vCenter username", required = true)
	private String username;

	@Parameter(names = { "-p", "--password" }, description = "vCenter password", required = true)
	private String password;

	@Parameter(names = { "-a", "--action" }, description = "Action to perform", required = true, converter = ActionConverter.class)
	private Action action;

	@Parameter(names = { "-l", "--line" }, description = "spec for one VM as a simple CSV line", required = false)
	private String line;

	@Parameter(names = { "-f", "--file" }, description = "CSV file", required = false)
	private File file;

	@Parameter(names = { "-t", "--template" }, description = "Name of the template to use - only valid for vm creation action", required = false)
	private String templateName;

	@Parameter(names = { "-e", "--post-exec" }, description = "Script to execute after VM creation - only valid for vm creation action", required = false)
	private File postExec;


	@Parameter(names = { "-h", "--help" }, description = "print help text", required = false)
	private boolean help;
}
