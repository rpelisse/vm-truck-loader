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

import static org.redhat.vmtruckloader.util.StringUtil.checkIfStringValid;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.redhat.vmtruckloader.service.MachineSpecification;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

/**
 * 
 * @author Romain Pelisse - romain@redhat.com
 *
 */
public final class CSVUtils {

	private static final char CSV_SEPARATOR = ';';
	
	private CSVUtils() {}
	
	private static String[] turnSpecIntoCsvLine(MachineSpecification spec, String[] line) {
		int i = 0;
		line[i++] = spec.getEnv();
		line[i++] = spec.getHostname();
		line[i++] = spec.getRole();
		line[i++] = spec.getMAC();
		line[i++] = spec.getIpAddress();
		line[i++] = spec.getVLAN();
		line[i++] = spec.getResourcePoolName();
		line[i++] = spec.getDatastoreName();
		line[i++] = String.valueOf(spec.getNbCpu());
		line[i++] = String.valueOf(spec.getVRAM());
		return line;
	}

	private static final String VALIDATION_ERROR_MESSAGE_PREFIX = "Invalid " + MachineSpecification.class.getSimpleName() +  " -";
	private static void validateSpec(MachineSpecification spec) {		
		checkIfStringValid(spec.getVmName(),VALIDATION_ERROR_MESSAGE_PREFIX +  " VM name is empty or 'null'.");
		checkIfStringValid(spec.getEnv(),VALIDATION_ERROR_MESSAGE_PREFIX +  " no Env specified for VM " + spec.getVmName());
		checkIfStringValid(spec.getRole(),VALIDATION_ERROR_MESSAGE_PREFIX +  "  no Role specified for VM " + spec.getVmName());
		checkIfStringValid(spec.getMAC(),VALIDATION_ERROR_MESSAGE_PREFIX +  "  no MAC Address specified for VM " + spec.getVmName());
		checkIfStringValid(spec.getVLAN(),VALIDATION_ERROR_MESSAGE_PREFIX +  "  no VLAN specified for VM " + spec.getVmName());
		checkIfStringValid(spec.getResourcePoolName(),VALIDATION_ERROR_MESSAGE_PREFIX +  "  no Resource Pool name specified for VM " + spec.getVmName());
		checkIfStringValid(spec.getDatastoreName(),VALIDATION_ERROR_MESSAGE_PREFIX +  "  no Datastore name specified for VM " + spec.getVmName());
		if ( spec.getNbCpu() <= 0 )
			throw new IllegalArgumentException(VALIDATION_ERROR_MESSAGE_PREFIX +  "  invalid number of CPUs:" + spec.getNbCpu() + " specified for VM " + spec.getVmName());
		if ( spec.getVRAM() <= 0 )
			throw new IllegalArgumentException(VALIDATION_ERROR_MESSAGE_PREFIX +  "  invalid value for RAM:" + spec.getVRAM() + " specified for VM " + spec.getVmName());
	}
	
	public static boolean updateLineInCsvFile(String filename , MachineSpecification spec) {
		validateSpec(spec);
		try {
			List<String[]> entries = new CSVReader(new FileReader(filename),CSV_SEPARATOR).readAll();
			
			CSVWriter writer = new CSVWriter(new FileWriter(filename), CSV_SEPARATOR);
		    for ( String[] entry : entries ) {
		    	if ( spec.getVmName().equals(entry[1]) )
		    		entry = turnSpecIntoCsvLine(spec, entry);
			     writer.writeNext(entry);		    	
		    }	
			writer.close();
		return true;
		} catch (IOException e ) {
			throw new IllegalStateException(e);
		}
	}
	
	public static MachineSpecification turnLineIntoSpec(String line) {
		ColumnPositionMappingStrategy<MachineSpecification> strategy = new ColumnPositionMappingStrategy<MachineSpecification>();
		strategy.setType(MachineSpecification.class);
		String[] columns = new String[] {"env", "hostname", "role", "MAC", "ipAddress", "VLAN", "resourcePoolName", "datastoreName", "nbCpu", "vRAM", "diskSize"}; 
		strategy.setColumnMapping(columns);

		CsvToBean<MachineSpecification> csv = new CsvToBean<MachineSpecification>();
		List<MachineSpecification> list = csv.parse(strategy, new CSVReader(new StringReader(line),CSV_SEPARATOR));
		if ( list == null || list.isEmpty() )
			throw new IllegalArgumentException("The following CSV line was successfully parsed but did not contained any data:" + line);
		return list.get(0);
	}

}
