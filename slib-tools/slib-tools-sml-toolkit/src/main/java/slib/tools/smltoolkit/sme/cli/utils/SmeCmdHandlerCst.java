/*

Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

This software is a computer program whose purpose is to 
process semantic graphs.

This software is governed by the CeCILL  license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.

 */
 
 
package slib.tools.smltoolkit.sme.cli.utils;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import slib.sml.sme.discriminative_power.DiscriminativePowerComputer;
import slib.tools.module.ToolCmdHandlerCst;
import slib.tools.smltoolkit.SmlToolKitCliCst;

public class SmeCmdHandlerCst extends ToolCmdHandlerCst{

	

	public static final String   moduleName 	  	= SmlToolKitCliCst.ToolName_SME;
	public static final String   appCmdName 	  	= SmlToolKitCliCst.appCmdName+" -module "+moduleName;
	public static boolean 		 debugMode  = false;
	
	public static final String   processDP 	  	= "discriminative_power";
	
	public static final String[] acceptedProcesses 	= {processDP};
	
	/*
	 * Error messages  
	 */
	
	public static final String errorMissingProcess = "[ERROR] Please specify a process to perform, available "+Arrays.toString(acceptedProcesses);
	
	/*
	 * Setting Options 
	 */
	
	public static Option help 			= new Option( "help", "print this message" );
	
	@SuppressWarnings("static-access")
	public static Option process  = OptionBuilder.withArgName( "process" )
	.hasArg()
	.withDescription( "process to perform (required) "+Arrays.toString(acceptedProcesses) )
	.create( "process" );

	@SuppressWarnings("static-access")
	public static Option sm_file  = OptionBuilder.withArgName( "sm_file" )
	.hasArg()
	.withDescription( "semantic measures result file (required for "+processDP+")" )
	.create( "sm_file" );
	
	@SuppressWarnings("static-access")
	public static Option clanFile  = OptionBuilder.withArgName( "clan_file" )
	.hasArg()
	.withDescription( "file containing clans (required for "+processDP+")" )
	.create( "clan_file" );
	
	@SuppressWarnings("static-access")
	public static Option output  = OptionBuilder.withArgName( "output" )
	.hasArg()
	.withDescription( "output file (required for "+processDP+")" )
	.create( "output" );
	
	@SuppressWarnings("static-access")
	public static Option max_value  = OptionBuilder.withArgName( "max_value" )
	.hasArg()
	.withDescription( "Decimal value. Set all discriminative power bigger than the specified maximal value to max_value ("+processDP+" optional, default "+DiscriminativePowerComputer.max_value_default+" )" )
	.create( "max_value" );
	
	/*
	 * Use this data structure to define order of options in help message
	 */
	public final static HashMap<Option,Integer> optionsOrder = new HashMap<Option,Integer>();
	static
	{
		optionsOrder.put(process, optionsOrder.size());
		optionsOrder.put(sm_file, optionsOrder.size());
		optionsOrder.put(clanFile, optionsOrder.size());
		optionsOrder.put(output, optionsOrder.size());
		optionsOrder.put(max_value, optionsOrder.size());
		optionsOrder.put(help, optionsOrder.size());
	 }
	
	
	public SmeCmdHandlerCst() {
		super(appCmdName, debugMode, optionsOrder);
	}
}
