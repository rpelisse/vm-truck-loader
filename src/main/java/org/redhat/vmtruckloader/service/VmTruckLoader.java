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

import static org.redhat.vmtruckloader.util.CSVUtils.turnLineIntoSpec;
import static org.redhat.vmtruckloader.util.CSVUtils.updateLineInCsvFile;
import static org.redhat.vmtruckloader.util.ExecUtils.COMMAND_LINE_SEPARATOR;
import static org.redhat.vmtruckloader.util.ExecUtils.executeScriptAndReturnsResult;
import static org.redhat.vmtruckloader.util.StringUtil.checkIfStringValid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.redhat.vmtruckloader.service.cli.Arguments;
import org.redhat.vmtruckloader.util.FileUtils;
import org.redhat.vmtruckloader.util.StringUtil;
import org.redhat.vmtruckloader.util.URLUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Main class for this tool:
 * Process the arguments, displays help text, and ensure the program
 * exit with the appropriate error code
 *
 * @author Romain Pelisse - <belaran@redhat.com>
 *
 */
public class VmTruckLoader {

	private static final String PROG_NAME = "vm-set-up";

	private static final int INVALID_COMMAND_INPUT = 1;
	private static final int INPUT_ERROR_NOFILE_NORLINE = 2;
	private static final int PROGRAM_THROWN_EXCEPTION = 3;


	private static final boolean STACKTRACE_ENABLED = false;

	public static void runWithCatch(String[] args) {
		try {
			run(args);
		} catch ( Throwable t ) {
			consolePrint(t.getMessage());
			if ( t.getCause() != null )
				consolePrint(t.getCause().getMessage());
			System.exit(PROGRAM_THROWN_EXCEPTION);
		}
	}

	public static void run(String[] args) {
		Arguments arguments = setDefaultValues(validateArgs(VmTruckLoader.extractParameters(args)));

		List<MachineSpecification> specs;
		if ( arguments.getFile() != null )
			specs = turnFileIntoSpec(arguments.getFile());
		else {
			specs = new ArrayList<MachineSpecification>();
			specs.add(turnLineIntoSpec(arguments.getLine()));
		}

		Properties props = System.getProperties();
		props.put("org.slf4j.simpleLogger.defaultLogLevel", "ERROR");
		System.setProperties(props);

		WeldContainer weld = new Weld().initialize();
		MachineService machineService = weld.instance().select(MachineService.class).get();
		machineService.setConnectionData(arguments.getUsername(), arguments.getPassword(), URLUtils.createUrl(arguments.getVCenterURl()));

		if ( specs.isEmpty() ) {
			consolePrint("No machine specification found.");
			System.exit(0);
		}

		validatePostExecScript(arguments.getPostExec());

		for ( MachineSpecification spec : specs ) {
			switch (arguments.getAction()) {
			case CREATE:
				StringUtil.checkIfStringValid(arguments.getTemplateName(),"The template name is required for action " + arguments.getAction());
				consolePrint("Creating machine " + spec.getVmName() + " ...");
				spec = machineService.cloneMachine(arguments.getTemplateName(), spec);
				spec.setMAC("@MAC");
				consolePrint("- machine " + spec.getVmName() + " has been successfully created." );
				consolePrint("-      with @MAC: " +	spec.getMAC());
                if ( arguments.getFile() != null )
				    updateLineInCsvFile(arguments.getFile().getAbsolutePath(), spec);
				runPostExecScriptIfAny(arguments.getPostExec(), spec);
				break;
			case DELETE:
				consolePrint("Deleting VM: " + spec.getHostname());
				machineService.deleteMachine(spec.getHostname());
				break;
			case START:
				consolePrint("Starting VM: " + spec.getHostname());
				machineService.startMachine(spec.getHostname());
				break;
			case STOP:
				consolePrint("Stopping VM: " + spec.getHostname());
				machineService.stopMachine(spec.getHostname());
				break;
			case RESTART:
				consolePrint("Restarting VM: " + spec.getHostname());
				machineService.stopMachine(spec.getHostname());
				machineService.startMachine(spec.getHostname());
				break;
			case EDIT:
				consolePrint("Edit VM: " + spec.getHostname());
				machineService.editMachine(spec);
				break;
			default:
				throw new IllegalStateException("Unrecognized action requested:" + arguments.getAction());
			}
		}
	}

	private static void runPostExecScriptIfAny(File postExec,
			MachineSpecification spec) {
		if ( postExec == null) return;
		for ( String line : executeScriptAndReturnsResult(buildCommandLine(postExec,spec)))
			consolePrint(line);
	}

	private static String buildCommandLine(File postExec,
			MachineSpecification spec) {

		return postExec.getAbsolutePath() + COMMAND_LINE_SEPARATOR + spec.getHostname() + COMMAND_LINE_SEPARATOR +
				spec.getMAC() + COMMAND_LINE_SEPARATOR + spec.getRole() + COMMAND_LINE_SEPARATOR + spec.getRole();
	}

	private static void validatePostExecScript(File postExec) {
		if ( postExec == null) return;

		if ( ! postExec.exists() )
	      throw new IllegalArgumentException(postExec.getAbsolutePath() + " does not exist, and therefore can't be run ");

		if ( ! postExec.canExecute() )
		      throw new IllegalArgumentException(postExec.getAbsolutePath() + " is not executable, and therefore can't be run ");
	}

	private static List<MachineSpecification> turnFileIntoSpec(File file) {
		List<String> lines =  FileUtils.readLineByLine(file);
		List<MachineSpecification> specs = new ArrayList<MachineSpecification>(lines.size());
		for ( String line : lines ) {
			specs.add(turnLineIntoSpec(line));
		}
		return specs;
	}

	public static void main(String[] args) {
		if ( STACKTRACE_ENABLED ) runWithCatch(args); else run(args);
	}

	private static Arguments validateArgs(Arguments arguments) {
		checkIfStringValid(arguments.getUsername(),"Username can't be 'null' or 'empty'");
		checkIfStringValid(arguments.getPassword(),"Password can't be 'null' or 'empty'");
		checkIfStringValid(arguments.getVCenterURl(),"vCenter URL can't be 'null' or 'empty'");
		return arguments;
	}

	private static Arguments setDefaultValues(Arguments arguments) {
		return arguments;
	}

	private static void consolePrint(String string, boolean printCarriageReturn) {
		if ( printCarriageReturn )
			System.out.println(string);	//NOPMD
		else
			System.out.print(string);	//NOPMD
	}

	private static void consolePrint(String string) {
		consolePrint(string, true);
	}

	private static Arguments extractParameters(String[] args) {
		Arguments arguments = new Arguments();
		JCommander jcommander = null;
		try {
			jcommander = new JCommander(arguments, args);
			jcommander.setProgramName(PROG_NAME);
			if (arguments.isHelp()) {
				jcommander.usage();
				System.exit(0);
			}

			if ( arguments.getLine() == null && arguments.getFile() == null ) {
				consolePrint(PROG_NAME + " requires either a line or a file:");
				jcommander.usage();
				System.exit(INPUT_ERROR_NOFILE_NORLINE);
			}

		} catch (ParameterException e) {
			System.out.println(e.getMessage());
			System.exit(INVALID_COMMAND_INPUT);
		}
		return arguments;
	}
}
